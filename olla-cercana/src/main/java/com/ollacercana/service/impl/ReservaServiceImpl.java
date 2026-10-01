package com.ollacercana.service.impl;

import com.ollacercana.model.dto.response.ReservaResponseDTO;
import com.ollacercana.exception.*;
import com.ollacercana.mapper.ReservaMapper;
import com.ollacercana.model.domain.*;
import com.ollacercana.model.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.model.dto.request.ReservaRequestDTO;
import com.ollacercana.observer.PublicadorEventosReserva;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReporteRepository;
import com.ollacercana.repository.ReservaRepository;
import com.ollacercana.service.ReservaService;
import com.ollacercana.validator.ReservaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final PlatoRepository platoRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;
    private final ReporteRepository reporteRepository;
    private final ReservaValidator validator;
    private final ReservaMapper reservaMapper;
    private final PublicadorEventosReserva publicador;

    // ============ HU-11: crear reserva ============

    @Override
    @Transactional
    public ReservaResponseDTO crear(Long compradorId, ReservaRequestDTO request) {
        Plato plato = platoRepository.findById(request.platoId())
                .orElseThrow(() -> new PlatoNoEncontradoException(request.platoId()));

        validator.validarParaCrear(compradorId, plato, request.cantidad());

        // Descontar porciones (RN-03)
        plato.comprometerPorciones(request.cantidad());
        try {
            platoRepository.saveAndFlush(plato);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConflictoException("El plato fue modificado por otra transacción simultánea");
        }

        // Crear la reserva (método de dominio)
        Reserva reserva = Reserva.crear(
                plato, compradorId, request.cantidad(),
                request.medioPago(), request.nota(), LocalDateTime.now());

        Reserva guardada = reservaRepository.save(reserva);

        // Publicar evento (Observer)
        publicador.publicar(EventoReserva.de(TipoEvento.RESERVA_CREADA, guardada, Map.of()));

        String conjunto = perfilCocineraRepository.findById(plato.getCocineraId())
                .map(PerfilCocinera::getConjuntoResidencial)
                .orElse("Conjunto Residencial");

        return reservaMapper.toResponse(guardada);
    }

    // ============ HU-12: decisión de la cocinera ============

    @Override
    @Transactional(readOnly = true)
    public Reserva obtenerPorId(UUID reservaId) {
        return buscar(reservaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> listarPendientesDeCocinera(UUID cocineraId) {
        LocalDateTime ahora = LocalDateTime.now();
        return reservaRepository
                .findByCocineraIdAndEstadoOrderByFechaLimiteConfirmacionAsc(cocineraId, EstadoReserva.PENDIENTE)
                .stream()
                .filter(r -> !r.estaVencida(ahora))
                .toList();
    }

    @Override
    @Transactional
    public Reserva decidir(UUID reservaId, UUID cocineraId, DecisionReservaRequestDTO request) {
        Reserva reserva = buscar(reservaId);
        if (!reserva.perteneceACocinera(cocineraId)) {
            throw new AccesoDenegadoException("Esta solicitud de reserva no pertenece a tu cocina");
        }

        return switch (request.decision()) {
            case CONFIRMAR -> confirmar(reserva, request.horaEstimada());
            case RECHAZAR -> rechazar(reserva, request.motivo(), request.comentario());
        };
    }

    // ============ RN-04 / RN-25: expiración y recordatorio ============

    @Override
    @Transactional(readOnly = true)
    public List<UUID> buscarReservasVencidas() {
        return reservaRepository
                .findByEstadoAndFechaLimiteConfirmacionLessThanEqual(
                        EstadoReserva.PENDIENTE, LocalDateTime.now())
                .stream()
                .map(Reserva::getId)
                .toList();
    }

    @Override
    @Transactional
    public Reserva expirar(UUID reservaId) {
        Reserva reserva = buscar(reservaId);
        LocalDateTime ahora = LocalDateTime.now();

        if (!reserva.estaPendiente() || !reserva.estaVencida(ahora)) {
            return reserva;
        }

        reserva.expirar(ahora);
        liberarPorciones(reserva);
        Reserva guardada = guardar(reserva);

        publicador.publicar(EventoReserva.de(TipoEvento.RESERVA_EXPIRADA, guardada, Map.of()));
        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> buscarReservasParaRecordatorio() {
        LocalDateTime ahora = LocalDateTime.now();
        return reservaRepository
                .findPendientesParaRecordatorio(
                        EstadoReserva.PENDIENTE,
                        ahora.minusMinutes(Reserva.MINUTOS_PARA_RECORDATORIO),
                        ahora)
                .stream()
                .map(Reserva::getId)
                .toList();
    }

    @Override
    @Transactional
    public void enviarRecordatorio(UUID reservaId) {
        Reserva reserva = buscar(reservaId);
        LocalDateTime ahora = LocalDateTime.now();

        if (!reserva.requiereRecordatorio(ahora)) {
            return;
        }

        reserva.marcarRecordatorioEnviado();
        Reserva guardada = guardar(reserva);

        long minutosRestantes = Math.max(1,
                Duration.between(ahora, reserva.getFechaLimiteConfirmacion()).toMinutes());
        publicador.publicar(EventoReserva.de(
                TipoEvento.RECORDATORIO_RESERVA, guardada,
                Map.of("minutosRestantes", minutosRestantes)));
    }

    // ============ HU-23: cierre de transacción ============

    @Override
    @Transactional
    public Reserva completar(UUID reservaId, String comentario) {
        Reserva reserva = buscar(reservaId);
        reserva.verificarQueEstaConfirmada();
        verificarSinReporteAbierto(reserva);
        return cerrar(reserva, comentario, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> buscarReservasParaCierreAutomatico() {
        LocalDateTime limite = LocalDateTime.now().minusHours(Reserva.HORAS_PARA_CIERRE_AUTOMATICO);
        return reservaRepository
                .findByEstadoAndFechaDecisionLessThanEqual(EstadoReserva.CONFIRMADA, limite)
                .stream()
                .map(Reserva::getId)
                .toList();
    }

    @Override
    @Transactional
    public Reserva completarAutomaticamente(UUID reservaId) {
        Reserva reserva = buscar(reservaId);
        if (!reserva.cierreAutomaticoVencido(LocalDateTime.now())) {
            return reserva;
        }
        if (tieneReporteAbierto(reserva)) {
            log.info("La reserva {} tiene un reporte abierto; no se completa automáticamente", reservaId);
            return reserva;
        }
        return cerrar(reserva, null, true);
    }

    // ============ Helpers privados ============

    private Reserva confirmar(Reserva reserva, LocalDateTime horaEstimada) {
        reserva.confirmar(horaEstimada, LocalDateTime.now());
        Reserva guardada = guardar(reserva);
        publicador.publicar(EventoReserva.de(
                TipoEvento.RESERVA_CONFIRMADA, guardada,
                Map.of("horaEstimada", guardada.getHoraEstimadaEntrega())));
        return guardada;
    }

    private Reserva rechazar(Reserva reserva, MotivoRechazo motivo, String comentario) {
        reserva.rechazar(motivo, comentario, LocalDateTime.now());
        liberarPorciones(reserva);
        Reserva guardada = guardar(reserva);

        Map<String, Object> payload = new HashMap<>();
        payload.put("motivo", guardada.getMotivoRechazo().getDescripcion());
        if (guardada.getComentarioRechazo() != null) {
            payload.put("comentario", guardada.getComentarioRechazo());
        }
        publicador.publicar(EventoReserva.de(TipoEvento.RESERVA_RECHAZADA, guardada, payload));
        return guardada;
    }

    private Reserva cerrar(Reserva reserva, String comentario, boolean automatica) {
        reserva.completar(comentario, LocalDateTime.now());
        Reserva guardada = guardar(reserva);
        publicador.publicar(EventoReserva.de(
                TipoEvento.RESERVA_COMPLETADA, guardada,
                Map.of("automatica", automatica)));
        return guardada;
    }

    private void verificarSinReporteAbierto(Reserva reserva) {
        if (tieneReporteAbierto(reserva)) {
            throw new ReglaDeNegocioException(
                    "No se puede cerrar la transacción: la reserva tiene un reporte abierto");
        }
    }

    private boolean tieneReporteAbierto(Reserva reserva) {
        return reporteRepository.existsByReservaIdAndEstado(reserva.getId(), EstadoReporte.ABIERTO);
    }

    private void liberarPorciones(Reserva reserva) {
        platoRepository.findById(reserva.getPlatoId()).ifPresentOrElse(
                plato -> {
                    plato.liberarPorciones(reserva.getCantidadPorciones());
                    platoRepository.save(plato);
                },
                () -> log.warn("El plato {} ya no existe; no hay porciones que devolver",
                        reserva.getPlatoId())
        );
    }

    private Reserva guardar(Reserva reserva) {
        try {
            return reservaRepository.saveAndFlush(reserva);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ReservaModificadaException();
        }
    }

    private Reserva buscar(UUID reservaId) {
        return reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNoEncontradaException(reservaId));
    }
}