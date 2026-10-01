package com.ollacercana.service;

import com.ollacercana.domain.*;
import com.ollacercana.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.dto.response.ReservaResponseDTO;
import com.ollacercana.exception.*;
import com.ollacercana.mapper.ReservaMapper;
import com.ollacercana.observer.PublicadorEventosReserva;
import com.ollacercana.repository.*;
import com.ollacercana.validator.ReservaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final PlatoRepository platoRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;
    private final ReporteRepository reporteRepository;
    private final EventoReservaRepository eventoReservaRepository;
    private final PublicadorEventosReserva publicador;
    private final ReservaValidator validator;
    private final ReservaMapper reservaMapper;

    // Constructor sobrecargado para soportar pruebas unitarias aisladas
    public ReservaServiceImpl(ReservaRepository reservaRepository, PlatoRepository platoRepository,
                              ReporteRepository reporteRepository, PublicadorEventosReserva publicador) {
        this(reservaRepository, platoRepository, null, reporteRepository, null, publicador, null, null);
    }

    @Override
    @Transactional
    public ReservaResponseDTO crear(Long compradorId, Reserva reserva) {
        Plato plato = platoRepository.findById(reserva.getPlatoId())
                .orElseThrow(() -> new PlatoNoEncontradoException(reserva.getPlatoId()));

        validator.validarParaCrear(compradorId, plato, reserva.getCantidadPorciones());

        plato.comprometerPorciones(reserva.getCantidadPorciones());

        try {
            platoRepository.saveAndFlush(plato);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConflictoException("El plato fue modificado por otra transacción simultánea, intenta de nuevo");
        }

        BigDecimal montoTotal = plato.getPrecioPorcion().multiply(BigDecimal.valueOf(reserva.getCantidadPorciones()));
        LocalDateTime ahora = LocalDateTime.now();

        reserva.setCocineraId(plato.getCocineraId());
        reserva.setCompradorId(compradorId);
        reserva.setMontoTotal(montoTotal);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setFechaCreacion(ahora);
        reserva.setFechaLimiteConfirmacion(ahora.plusMinutes(Reserva.MINUTOS_PARA_CONFIRMAR));

        Reserva guardada = reservaRepository.save(reserva);

        EventoReserva evento = EventoReserva.builder()
                .tipo(TipoEvento.RESERVA_CREADA)
                .reservaId(guardada.getId())
                .platoId(plato.getId())
                .compradorId(compradorId)
                .cocineraId(plato.getCocineraId())
                .timestamp(ahora)
                .payload(Map.of("porciones", reserva.getCantidadPorciones(), "monto", montoTotal))
                .payloadJson("Reserva creada por " + reserva.getCantidadPorciones() + " porciones. Monto: " + montoTotal)
                .build();

        if (eventoReservaRepository != null) {
            eventoReservaRepository.save(evento);
        }
        if (publicador != null) {
            publicador.publicar(evento);
        }

        String conjunto = perfilCocineraRepository != null
                ? perfilCocineraRepository.findById(plato.getCocineraId())
                .map(PerfilCocinera::getConjuntoResidencial)
                .orElse("Conjunto Residencial")
                : "Conjunto Residencial";

        return reservaMapper.toResponseDTO(guardada, plato.getNombre(), conjunto);
    }

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
                .filter(reserva -> !reserva.estaVencida(ahora))
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

    @Override
    @Transactional
    public Reserva confirmar(UUID reservaId, LocalDateTime horaEstimada) {
        return confirmar(buscar(reservaId), horaEstimada);
    }

    @Override
    @Transactional
    public Reserva rechazar(UUID reservaId, MotivoRechazo motivo, String comentario) {
        return rechazar(buscar(reservaId), motivo, comentario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> buscarReservasVencidas() {
        return reservaRepository
                .findByEstadoAndFechaLimiteConfirmacionLessThanEqual(EstadoReserva.PENDIENTE, LocalDateTime.now())
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

        if (publicador != null) {
            publicador.publicar(EventoReserva.de(TipoEvento.RESERVA_EXPIRADA, guardada, Map.of()));
        }
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

        long minutosRestantes = Math.max(1, Duration.between(ahora, reserva.getFechaLimiteConfirmacion()).toMinutes());
        if (publicador != null) {
            publicador.publicar(EventoReserva.de(
                    TipoEvento.RECORDATORIO_RESERVA, guardada, Map.of("minutosRestantes", minutosRestantes)));
        }
    }

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

    private Reserva cerrar(Reserva reserva, String comentario, boolean automatica) {
        reserva.completar(comentario, LocalDateTime.now());
        Reserva guardada = guardar(reserva);

        if (publicador != null) {
            publicador.publicar(EventoReserva.de(
                    TipoEvento.RESERVA_COMPLETADA, guardada, Map.of("automatica", automatica)));
        }
        return guardada;
    }

    private void verificarSinReporteAbierto(Reserva reserva) {
        if (tieneReporteAbierto(reserva)) {
            throw new ReglaDeNegocioException(
                    "No se puede cerrar la transacción: la reserva tiene un reporte abierto pendiente de moderación");
        }
    }

    private boolean tieneReporteAbierto(Reserva reserva) {
        return reporteRepository != null && reporteRepository.existsByReservaIdAndEstado(reserva.getId(), EstadoReporte.ABIERTO);
    }

    private Reserva confirmar(Reserva reserva, LocalDateTime horaEstimada) {
        reserva.confirmar(horaEstimada, LocalDateTime.now());
        Reserva guardada = guardar(reserva);

        if (publicador != null) {
            publicador.publicar(EventoReserva.de(
                    TipoEvento.RESERVA_CONFIRMADA, guardada, Map.of("horaEstimada", guardada.getHoraEstimadaEntrega())));
        }
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
        if (publicador != null) {
            publicador.publicar(EventoReserva.de(TipoEvento.RESERVA_RECHAZADA, guardada, payload));
        }
        return guardada;
    }

    private void liberarPorciones(Reserva reserva) {
        platoRepository.findById(reserva.getPlatoId()).ifPresentOrElse(
                plato -> {
                    plato.liberarPorciones(reserva.getCantidadPorciones());
                    platoRepository.save(plato);
                },
                () -> log.warn("El plato {} de la reserva {} ya no existe; no hay porciones que devolver",
                        reserva.getPlatoId(), reserva.getId())
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