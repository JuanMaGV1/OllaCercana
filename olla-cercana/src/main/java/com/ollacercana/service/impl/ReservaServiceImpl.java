package com.ollacercana.service.impl;

import com.ollacercana.exception.*;
import com.ollacercana.mapper.ReservaEntityMapper;
import com.ollacercana.mapper.ReservaMapper;
import com.ollacercana.model.domain.*;
import com.ollacercana.model.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.model.dto.request.ReservaRequestDTO;
import com.ollacercana.model.dto.response.ReservaResponseDTO;
import com.ollacercana.observer.PublicadorEventosReserva;
import com.ollacercana.persistence.entity.PlatoEntity;
import com.ollacercana.persistence.entity.ReservaEntity;
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
    private final ReservaEntityMapper entityMapper;
    private final ReservaMapper reservaMapper;
    private final PublicadorEventosReserva publicador;

    @Override
    @Transactional
    public ReservaResponseDTO crear(UUID compradorId, ReservaRequestDTO request) {
        // 1. Obtener plato
        PlatoEntity platoEntity = platoRepository.findById(request.platoId())
                .orElseThrow(() -> new PlatoNoEncontradoException(request.platoId()));

        // Mapear a dominio para usar métodos de negocio
        Plato plato = Plato.builder()
                .id(platoEntity.getId())
                .cocineraId(platoEntity.getCocineraId())
                .nombre(platoEntity.getNombre())
                .precioPorcion(platoEntity.getPrecioPorcion())
                .porcionesTotales(platoEntity.getPorcionesTotales())
                .porcionesComprometidas(platoEntity.getPorcionesComprometidas())
                .estado(platoEntity.getEstado())
                .build();

        // 2. Validar reglas
        validator.validarParaCrear(compradorId, plato, request.cantidad());

        // 3. Descontar porciones
        plato.comprometerPorciones(request.cantidad());
        platoEntity.setPorcionesTotales(plato.getPorcionesTotales());
        platoEntity.setPorcionesComprometidas(plato.getPorcionesComprometidas());
        platoEntity.setEstado(plato.getEstado());

        try {
            platoRepository.saveAndFlush(platoEntity);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConflictoException("El plato fue modificado simultáneamente");
        }

        // 4. Crear reserva
        Reserva reserva = Reserva.crear(plato, compradorId, request.cantidad(),
                request.medioPago(), request.nota(), LocalDateTime.now());

        ReservaEntity entity = entityMapper.toEntity(reserva);
        ReservaEntity guardada = reservaRepository.save(entity);

        // 5. Publicar evento
        Reserva reservaGuardada = entityMapper.toDomain(guardada);
        publicador.publicar(EventoReserva.de(TipoEvento.RESERVA_CREADA, reservaGuardada, Map.of()));

        return reservaMapper.toResponse(reservaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Reserva obtenerPorId(UUID reservaId) {
        return entityMapper.toDomain(buscar(reservaId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> listarPendientesDeCocinera(UUID cocineraId) {
        LocalDateTime ahora = LocalDateTime.now();
        return reservaRepository
                .findByCocineraIdAndEstadoOrderByFechaLimiteConfirmacionAsc(cocineraId, EstadoReserva.PENDIENTE)
                .stream()
                .map(entityMapper::toDomain)
                .filter(r -> !r.estaVencida(ahora))
                .toList();
    }

    @Override
    @Transactional
    public Reserva decidir(UUID reservaId, UUID cocineraId, DecisionReservaRequestDTO request) {
        Reserva reserva = entityMapper.toDomain(buscar(reservaId));
        if (!reserva.perteneceACocinera(cocineraId)) {
            throw new AccesoDenegadoException("Esta reserva no pertenece a tu cocina");
        }
        return switch (request.decision()) {
            case CONFIRMAR -> confirmar(reserva, request.horaEstimada());
            case RECHAZAR -> rechazar(reserva, request.motivo(), request.comentario());
        };
    }

    @Override
    @Transactional
    public Reserva completar(UUID reservaId, String comentario) {
        Reserva reserva = entityMapper.toDomain(buscar(reservaId));
        reserva.verificarQueEstaConfirmada();
        verificarSinReporteAbierto(reserva);
        return cerrar(reserva, comentario, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> buscarReservasVencidas() {
        return reservaRepository
                .findByEstadoAndFechaLimiteConfirmacionLessThanEqual(EstadoReserva.PENDIENTE, LocalDateTime.now())
                .stream().map(ReservaEntity::getId).toList();
    }

    @Override
    @Transactional
    public Reserva expirar(UUID reservaId) {
        Reserva reserva = entityMapper.toDomain(buscar(reservaId));
        LocalDateTime ahora = LocalDateTime.now();
        if (!reserva.estaPendiente() || !reserva.estaVencida(ahora)) return reserva;

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
                .findPendientesParaRecordatorio(EstadoReserva.PENDIENTE,
                        ahora.minusMinutes(Reserva.MINUTOS_PARA_RECORDATORIO), ahora)
                .stream().map(ReservaEntity::getId).toList();
    }

    @Override
    @Transactional
    public void enviarRecordatorio(UUID reservaId) {
        Reserva reserva = entityMapper.toDomain(buscar(reservaId));
        LocalDateTime ahora = LocalDateTime.now();
        if (!reserva.requiereRecordatorio(ahora)) return;

        reserva.marcarRecordatorioEnviado();
        Reserva guardada = guardar(reserva);
        long minutosRestantes = Math.max(1,
                Duration.between(ahora, reserva.getFechaLimiteConfirmacion()).toMinutes());
        publicador.publicar(EventoReserva.de(TipoEvento.RECORDATORIO_RESERVA, guardada,
                Map.of("minutosRestantes", minutosRestantes)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> buscarReservasParaCierreAutomatico() {
        LocalDateTime limite = LocalDateTime.now().minusHours(Reserva.HORAS_PARA_CIERRE_AUTOMATICO);
        return reservaRepository
                .findByEstadoAndFechaDecisionLessThanEqual(EstadoReserva.CONFIRMADA, limite)
                .stream().map(ReservaEntity::getId).toList();
    }

    @Override
    @Transactional
    public Reserva completarAutomaticamente(UUID reservaId) {
        Reserva reserva = entityMapper.toDomain(buscar(reservaId));
        if (!reserva.cierreAutomaticoVencido(LocalDateTime.now())) return reserva;
        if (tieneReporteAbierto(reserva)) {
            log.info("Reserva {} con reporte abierto", reservaId);
            return reserva;
        }
        return cerrar(reserva, null, true);
    }

    // ============ Helpers ============

    private Reserva confirmar(Reserva reserva, LocalDateTime horaEstimada) {
        reserva.confirmar(horaEstimada, LocalDateTime.now());
        Reserva guardada = guardar(reserva);
        publicador.publicar(EventoReserva.de(TipoEvento.RESERVA_CONFIRMADA, guardada,
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
        publicador.publicar(EventoReserva.de(TipoEvento.RESERVA_COMPLETADA, guardada,
                Map.of("automatica", automatica)));
        return guardada;
    }

    private void verificarSinReporteAbierto(Reserva reserva) {
        if (tieneReporteAbierto(reserva)) {
            throw new ReglaDeNegocioException(
                    "No se puede cerrar la transacción: hay un reporte abierto");
        }
    }

    private boolean tieneReporteAbierto(Reserva reserva) {
        return reporteRepository.existsByReservaIdAndEstado(reserva.getId(), EstadoReporte.ABIERTO);
    }

    private void liberarPorciones(Reserva reserva) {
        platoRepository.findById(reserva.getPlatoId()).ifPresentOrElse(platoEntity -> {
            Plato plato = Plato.builder()
                    .id(platoEntity.getId())
                    .porcionesTotales(platoEntity.getPorcionesTotales())
                    .porcionesComprometidas(platoEntity.getPorcionesComprometidas())
                    .estado(platoEntity.getEstado())
                    .build();
            plato.liberarPorciones(reserva.getCantidadPorciones());
            platoEntity.setPorcionesComprometidas(plato.getPorcionesComprometidas());
            platoEntity.setEstado(plato.getEstado());
            platoRepository.save(platoEntity);
        }, () -> log.warn("El plato {} ya no existe", reserva.getPlatoId()));
    }

    private Reserva guardar(Reserva reserva) {
        try {
            ReservaEntity entity = entityMapper.toEntity(reserva);
            ReservaEntity guardada = reservaRepository.saveAndFlush(entity);
            return entityMapper.toDomain(guardada);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ReservaModificadaException();
        }
    }

    private ReservaEntity buscar(UUID reservaId) {
        return reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNoEncontradaException(reservaId));
    }
}