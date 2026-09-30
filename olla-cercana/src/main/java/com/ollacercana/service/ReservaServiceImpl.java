package com.ollacercana.service;

import com.ollacercana.domain.EstadoReporte;
import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.EventoReserva;
import com.ollacercana.domain.MotivoRechazo;
import com.ollacercana.domain.Reserva;
import com.ollacercana.domain.TipoEvento;
import com.ollacercana.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.exception.AccesoDenegadoException;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.exception.ReservaModificadaException;
import com.ollacercana.exception.ReservaNoEncontradaException;
import com.ollacercana.observer.PublicadorEventosReserva;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReporteRepository;
import com.ollacercana.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements com.ollacercana.service.ReservaService {

    private static final Logger log = LoggerFactory.getLogger(ReservaServiceImpl.class);

    private final ReservaRepository reservaRepository;
    private final PlatoRepository platoRepository;
    private final ReporteRepository reporteRepository;
    private final PublicadorEventosReserva publicador;

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

        long minutosRestantes = Math.max(1, Duration.between(ahora, reserva.getFechaLimiteConfirmacion()).toMinutes());
        publicador.publicar(EventoReserva.de(
                TipoEvento.RECORDATORIO_RESERVA, guardada, Map.of("minutosRestantes", minutosRestantes)));
    }

    @Override
    @Transactional
    public Reserva completar(UUID reservaId, String comentario) {
        Reserva reserva = buscar(reservaId);
        // Escenario 4: si no está CONFIRMADA se rechaza antes de mirar cualquier otra cosa.
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

    // ============ Lógica interna ============

    private Reserva cerrar(Reserva reserva, String comentario, boolean automatica) {
        reserva.completar(comentario, LocalDateTime.now());
        Reserva guardada = guardar(reserva);

        publicador.publicar(EventoReserva.de(
                TipoEvento.RESERVA_COMPLETADA, guardada, Map.of("automatica", automatica)));
        return guardada;
    }

    private void verificarSinReporteAbierto(Reserva reserva) {
        if (tieneReporteAbierto(reserva)) {
            throw new ReglaDeNegocioException(
                    "No se puede cerrar la transacción: la reserva tiene un reporte abierto pendiente de moderación");
        }
    }

    private boolean tieneReporteAbierto(Reserva reserva) {
        return reporteRepository.existsByReservaIdAndEstado(reserva.getId(), EstadoReporte.ABIERTO);
    }

    private Reserva confirmar(Reserva reserva, LocalDateTime horaEstimada) {
        // Las porciones ya se descontaron al crear la reserva: al confirmar se conservan.
        reserva.confirmar(horaEstimada, LocalDateTime.now());
        Reserva guardada = guardar(reserva);

        publicador.publicar(EventoReserva.de(
                TipoEvento.RESERVA_CONFIRMADA, guardada, Map.of("horaEstimada", guardada.getHoraEstimadaEntrega())));
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

    /**
     * RN-03: devuelve al plato las porciones que tenía comprometidas la reserva.
     * Si el plato ya no existe (fue eliminado) no hay nada que devolver.
     */
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
