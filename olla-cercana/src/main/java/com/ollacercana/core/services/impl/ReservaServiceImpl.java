package com.ollacercana.core.services.impl;

import com.ollacercana.controller.dtos.request.DecisionReservaRequestDTO;
import com.ollacercana.controller.handlers.exception.*;
import com.ollacercana.core.models.EventoReserva;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.Reserva;
import com.ollacercana.core.models.enums.EstadoReporte;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.models.enums.MotivoRechazo;
import com.ollacercana.core.models.enums.TipoEvento;
import com.ollacercana.core.patterns.observer.PublicadorEventosPorciones;
import com.ollacercana.core.patterns.observer.PublicadorEventosReserva;
import com.ollacercana.core.services.ReservaService;
import com.ollacercana.core.validators.ReservaValidator;
import com.ollacercana.core.models.*;
import com.ollacercana.core.models.enums.*;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.entities.ReservaEntity;
import com.ollacercana.persistence.mappers.EventoMapper;
import com.ollacercana.persistence.mappers.PlatoEntityMapper;
import com.ollacercana.persistence.mappers.ReservaEntityMapper;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.PlatoRepository;
import com.ollacercana.persistence.repository.ReporteRepository;
import com.ollacercana.persistence.repository.ReservaRepository;
import com.ollacercana.persistence.repository.mongo.EventoReservaRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final PlatoRepository platoRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;
    private final ReporteRepository reporteRepository;
    private final EventoReservaRepository eventoReservaRepository;
    private final PublicadorEventosReserva publicador;
    private final ReservaValidator validator;
    private final ReservaEntityMapper reservaEntityMapper;
    private final PlatoEntityMapper platoEntityMapper;
    private PublicadorEventosPorciones publicadorEventosPorciones;
    private final EventoMapper eventoMapper;

    @Autowired(required = false)
    public void setPublicadorEventosPorciones(PublicadorEventosPorciones p) {
        this.publicadorEventosPorciones = p;
    }

    @Autowired
    public ReservaServiceImpl(ReservaRepository reservaRepository,
                              PlatoRepository platoRepository,
                              PerfilCocineraRepository perfilCocineraRepository,
                              ReporteRepository reporteRepository,
                              EventoReservaRepository eventoReservaRepository,
                              PublicadorEventosReserva publicador,
                              ReservaValidator validator,
                              ReservaEntityMapper reservaEntityMapper,
                              PlatoEntityMapper platoEntityMapper,
                              EventoMapper eventoMapper) {
        this.reservaRepository = reservaRepository;
        this.platoRepository = platoRepository;
        this.perfilCocineraRepository = perfilCocineraRepository;
        this.reporteRepository = reporteRepository;
        this.eventoReservaRepository = eventoReservaRepository;
        this.publicador = publicador;
        this.validator = validator;
        this.reservaEntityMapper = reservaEntityMapper;
        this.platoEntityMapper = platoEntityMapper;
        this.eventoMapper = eventoMapper;
    }

    @Override
    @Transactional
    public Reserva crear(Long compradorId, Reserva reserva) {
        PlatoEntity platoEntity = platoRepository.findById(reserva.getPlatoId())
                .orElseThrow(() -> new PlatoNoEncontradoException(reserva.getPlatoId()));
        Plato plato = platoEntityMapper.toDomain(platoEntity);

        if (validator != null) validator.validarParaCrear(compradorId, plato, reserva.getCantidadPorciones());

        int disponiblesAntes = plato.getPorcionesDisponibles();
        plato.comprometerPorciones(reserva.getCantidadPorciones());

        try {
            platoRepository.saveAndFlush(platoEntityMapper.toEntity(plato));
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConflictoException("El plato fue modificado por otra transacción simultánea, intenta de nuevo");
        }
        publicarPorciones(disponiblesAntes, plato);

        LocalDateTime ahora = LocalDateTime.now();
        Reserva nueva = Reserva.crear(plato, compradorId, reserva.getCantidadPorciones(),
                reserva.getMedioPago(), reserva.getNotaComprador(), ahora);
        if (reserva.getId() != null) nueva.setId(reserva.getId());

        ReservaEntity guardada = reservaRepository.save(reservaEntityMapper.toEntity(nueva));
        Reserva dominioGuardado = reservaEntityMapper.toDomain(guardada);

        EventoReserva evento = EventoReserva.de(TipoEvento.RESERVA_CREADA, dominioGuardado,
                Map.of("porciones", dominioGuardado.getCantidadPorciones(),
                        "monto", dominioGuardado.getMontoTotal()));

        if (eventoReservaRepository != null) {
        try {
            eventoReservaRepository.save(eventoMapper.toDocument(evento));
        } catch (Exception e) {
            log.warn("No se pudo registrar evento en Mongo: {}", e.getMessage());
        }
    }
        if (publicador != null) publicador.publicar(evento);

        return dominioGuardado;
    }

    @Override
    @Transactional(readOnly = true)
    public Reserva obtenerPorId(UUID reservaId) { return buscar(reservaId); }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> listarPendientesDeCocinera(UUID cocineraId) {
        LocalDateTime ahora = LocalDateTime.now();
        return reservaRepository
                .findByCocineraIdAndEstadoOrderByFechaLimiteConfirmacionAsc(cocineraId, EstadoReserva.PENDIENTE)
                .stream()
                .map(reservaEntityMapper::toDomain)
                .filter(r -> !r.estaVencida(ahora))
                .toList();
    }

    @Override
    @Transactional
    public Reserva decidir(UUID reservaId, UUID cocineraId, DecisionReservaRequestDTO request) {
        Reserva reserva = buscar(reservaId);
        if (!reserva.perteneceACocinera(cocineraId))
            throw new AccesoDenegadoException("Esta solicitud de reserva no pertenece a tu cocina");

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
                .stream().map(ReservaEntity::getId).toList();
    }

    @Override
    @Transactional
    public Reserva expirar(UUID reservaId) {
        Reserva reserva = buscar(reservaId);
        LocalDateTime ahora = LocalDateTime.now();
        if (!reserva.estaPendiente() || !reserva.estaVencida(ahora)) return reserva;

        reserva.expirar(ahora);
        liberarPorciones(reserva);
        ReservaEntity guardada = reservaRepository.save(reservaEntityMapper.toEntity(reserva));
        Reserva dominio = reservaEntityMapper.toDomain(guardada);

        if (publicador != null)
            publicador.publicar(EventoReserva.de(TipoEvento.RESERVA_EXPIRADA, dominio, Map.of()));
        return dominio;
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
                .stream().map(ReservaEntity::getId).toList();
    }

    @Override
    @Transactional
    public void enviarRecordatorio(UUID reservaId) {
        Reserva reserva = buscar(reservaId);
        LocalDateTime ahora = LocalDateTime.now();
        if (!reserva.requiereRecordatorio(ahora)) return;

        reserva.marcarRecordatorioEnviado();
        ReservaEntity guardada = reservaRepository.save(reservaEntityMapper.toEntity(reserva));
        Reserva dominio = reservaEntityMapper.toDomain(guardada);

        long minutos = Math.max(1, Duration.between(ahora, reserva.getFechaLimiteConfirmacion()).toMinutes());
        if (publicador != null)
            publicador.publicar(EventoReserva.de(TipoEvento.RECORDATORIO_RESERVA, dominio,
                    Map.of("minutosRestantes", minutos)));
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
        return reservaRepository.findByEstadoAndFechaDecisionLessThanEqual(EstadoReserva.CONFIRMADA, limite)
                .stream().map(ReservaEntity::getId).toList();
    }

    @Override
    @Transactional
    public Reserva completarAutomaticamente(UUID reservaId) {
        Reserva reserva = buscar(reservaId);
        if (!reserva.cierreAutomaticoVencido(LocalDateTime.now())) return reserva;
        if (tieneReporteAbierto(reserva)) return reserva;
        return cerrar(reserva, null, true);
    }

    private Reserva cerrar(Reserva reserva, String comentario, boolean automatica) {
        reserva.completar(comentario, LocalDateTime.now());
        ReservaEntity guardada = reservaRepository.save(reservaEntityMapper.toEntity(reserva));
        Reserva dominio = reservaEntityMapper.toDomain(guardada);

        if (publicador != null)
            publicador.publicar(EventoReserva.de(TipoEvento.RESERVA_COMPLETADA, dominio,
                    Map.of("automatica", automatica)));
        return dominio;
    }

    private void verificarSinReporteAbierto(Reserva reserva) {
        if (tieneReporteAbierto(reserva))
            throw new ReglaDeNegocioException(
                    "No se puede cerrar la transacción: la reserva tiene un reporte abierto pendiente de moderación");
    }

    private boolean tieneReporteAbierto(Reserva reserva) {
        return reporteRepository != null
                && reporteRepository.existsByReservaIdAndEstado(reserva.getId(), EstadoReporte.ABIERTO);
    }

    private Reserva confirmar(Reserva reserva, LocalDateTime horaEstimada) {
        reserva.confirmar(horaEstimada, LocalDateTime.now());
        ReservaEntity guardada = reservaRepository.save(reservaEntityMapper.toEntity(reserva));
        Reserva dominio = reservaEntityMapper.toDomain(guardada);

        if (publicador != null)
            publicador.publicar(EventoReserva.de(TipoEvento.RESERVA_CONFIRMADA, dominio,
                    Map.of("horaEstimada", dominio.getHoraEstimadaEntrega())));
        return dominio;
    }

    private Reserva rechazar(Reserva reserva, MotivoRechazo motivo, String comentario) {
        reserva.rechazar(motivo, comentario, LocalDateTime.now());
        liberarPorciones(reserva);
        ReservaEntity guardada = reservaRepository.save(reservaEntityMapper.toEntity(reserva));
        Reserva dominio = reservaEntityMapper.toDomain(guardada);

        Map<String, Object> payload = new HashMap<>();
        payload.put("motivo", dominio.getMotivoRechazo().getDescripcion());
        if (dominio.getComentarioRechazo() != null) {
            payload.put("comentario", dominio.getComentarioRechazo());
        }

        if (publicador != null)
            publicador.publicar(EventoReserva.de(TipoEvento.RESERVA_RECHAZADA, dominio, payload));
        return dominio;
    }

    private void liberarPorciones(Reserva reserva) {
        platoRepository.findById(reserva.getPlatoId()).ifPresentOrElse(
                platoEntity -> {
                    Plato plato = platoEntityMapper.toDomain(platoEntity);
                    int antes = plato.getPorcionesDisponibles();
                    plato.liberarPorciones(reserva.getCantidadPorciones());
                    platoRepository.save(platoEntityMapper.toEntity(plato));
                    publicarPorciones(antes, plato);
                },
                () -> log.warn("El plato {} de la reserva {} ya no existe; no hay porciones que devolver",
                        reserva.getPlatoId(), reserva.getId())
        );
    }

    private void publicarPorciones(int disponiblesAntes, Plato plato) {
        if (publicadorEventosPorciones != null)
            publicadorEventosPorciones.publicarSiCambio(disponiblesAntes, plato);
    }

    private Reserva buscar(UUID reservaId) {
        return reservaRepository.findById(reservaId)
                .map(reservaEntityMapper::toDomain)
                .orElseThrow(() -> new ReservaNoEncontradaException(reservaId));
    }
}