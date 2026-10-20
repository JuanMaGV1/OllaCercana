package com.ollacercana.core.patterns.observer;

import com.ollacercana.core.models.EventoReserva;
import com.ollacercana.core.models.Notificacion;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.models.enums.TipoNotificacion;
import com.ollacercana.persistence.entities.CuentaEntity;
import com.ollacercana.persistence.mappers.NotificacionDocumentMapper;
import com.ollacercana.persistence.repository.CuentaRepository;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.mongo.NotificacionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class NotificacionInAppObservador implements ObservadorReserva {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificacionRepository notificacionRepository;
    private final NotificacionDocumentMapper notificacionMapper;
    private final CuentaRepository cuentaRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;

    @Autowired
    public NotificacionInAppObservador(
            NotificacionRepository notificacionRepository,
            NotificacionDocumentMapper notificacionMapper,
            @Autowired(required = false) CuentaRepository cuentaRepository,
            @Autowired(required = false) PerfilCocineraRepository perfilCocineraRepository) {
        this.notificacionRepository = notificacionRepository;
        this.notificacionMapper = notificacionMapper;
        this.cuentaRepository = cuentaRepository;
        this.perfilCocineraRepository = perfilCocineraRepository;
    }

    @Override
    public void notificar(EventoReserva evento) {
        List<Notificacion> notificaciones = switch (evento.tipo()) {
            case RESERVA_CONFIRMADA -> List.of(paraComprador(evento, TipoNotificacion.RESERVA_CONFIRMADA,
                    "¡Tu reserva fue confirmada!",
                    "La cocinera confirmó tu pedido. Hora estimada de entrega: " + horaEstimada(evento)
                            + ". Ya puedes coordinar la entrega."));
            case RESERVA_RECHAZADA -> List.of(paraComprador(evento, TipoNotificacion.RESERVA_RECHAZADA,
                    "Tu reserva fue rechazada",
                    "La cocinera no puede atender tu pedido. Motivo: " + motivo(evento)
                            + ". Las porciones volvieron a estar disponibles."));
            case RESERVA_EXPIRADA -> List.of(paraComprador(evento, TipoNotificacion.RESERVA_EXPIRADA,
                    "Tu reserva expiró",
                    "La cocinera no respondió a tiempo y la solicitud se canceló. "
                            + "Puedes buscar otro plato cerca de ti."));
            case RECORDATORIO_RESERVA -> {
                boolean esRecogida = Boolean.TRUE.equals(evento.payload().get("recordatorioRecogida"));
                if (esRecogida) {
                    yield List.of(paraComprador(evento, TipoNotificacion.RECORDATORIO,
                            "Tu pedido está por recogerse",
                            "Tu pedido está por recogerse en 15 minutos. Acércate al punto de entrega."));
                } else {
                    yield List.of(paraCocinera(evento, TipoNotificacion.RECORDATORIO,
                            "Tienes una solicitud sin responder",
                            "Te quedan " + evento.payload().getOrDefault("minutosRestantes", "pocos")
                                    + " minutos para confirmar o rechazar la reserva. Si no respondes, expirará automáticamente."));
                }
            }
            case RESERVA_COMPLETADA -> {
                String mensaje = mensajeCierre(evento);
                yield List.of(
                        paraComprador(evento, TipoNotificacion.INVITACION_CALIFICAR,
                                "Tu pedido fue completado", mensaje),
                        paraCocinera(evento, TipoNotificacion.INVITACION_CALIFICAR,
                                "Tu pedido fue completado", mensaje));
            }
            default -> List.of();
        };

        if (notificacionRepository != null) {
            try {
                for (Notificacion n : notificaciones) {
                    // 1. Siempre se guarda en MongoDB (Historial in-app)
                    notificacionRepository.save(notificacionMapper.toDocument(n));

                    // 2. Regla OC-294: Respetar si el usuario tiene activos los avisos para la push
                    despacharPushSiActivo(n);
                }
            } catch (Exception e) {
                log.warn("No se pudo persistir la notificación en Mongo: {}", e.getMessage());
            }
        }
    }

    private void despacharPushSiActivo(Notificacion n) {
        if (cuentaRepository == null) {
            return;
        }

        Long destinatarioId = (n.getRolDestinatario() == Rol.COMPRADOR)
                ? n.getCompradorId()
                : resolverCuentaIdDeCocinera(n.getCocineraId());

        if (destinatarioId != null) {
            boolean avisosActivos = cuentaRepository.findById(destinatarioId)
                    .map(CuentaEntity::getAvisosActivos)
                    .orElse(true);

            if (avisosActivos) {
                log.info("[PUSH DISPATCHER] Enviando notificación push a usuario {}: '{}'", destinatarioId, n.getTitulo());
            } else {
                log.info("[PUSH OMITIDA - OC-294] Usuario {} tiene avisos desactivados. Notificación guardada solo en historial.", destinatarioId);
            }
        }
    }

    private Long resolverCuentaIdDeCocinera(UUID cocineraId) {
        if (cocineraId == null || perfilCocineraRepository == null) {
            return null;
        }
        return perfilCocineraRepository.findById(cocineraId)
                .map(p -> p.getCuenta() != null ? p.getCuenta().getId() : null)
                .orElse(null);
    }

    private String mensajeCierre(EventoReserva evento) {
        boolean automatica = Boolean.TRUE.equals(evento.payload().get("automatica"));
        String origen = automatica
                ? "Pasaron 24 horas desde la confirmación y la reserva se completó automáticamente. "
                : "Se confirmó la entrega y el pago. ";
        return origen + "El chat quedó en solo lectura. ¡Ya puedes calificar la experiencia!";
    }

    private Notificacion paraComprador(EventoReserva evento, TipoNotificacion tipo, String titulo, String mensaje) {
        return crearNotificacion(evento, tipo, titulo, mensaje, Rol.COMPRADOR, evento.compradorId(), null);
    }

    private Notificacion paraCocinera(EventoReserva evento, TipoNotificacion tipo, String titulo, String mensaje) {
        return crearNotificacion(evento, tipo, titulo, mensaje, Rol.COCINERA, null, evento.cocineraId());
    }

    private Notificacion crearNotificacion(EventoReserva evento, TipoNotificacion tipo, String titulo, String mensaje,
                                           Rol rol, Long compradorId, UUID cocineraId) {
        return Notificacion.builder()
                .reservaId(evento.reservaId())
                .rolDestinatario(rol)
                .compradorId(compradorId)
                .cocineraId(cocineraId)
                .tipo(tipo)
                .titulo(titulo)
                .mensaje(mensaje)
                .leida(false)
                .fechaCreacion(LocalDateTime.now(java.time.ZoneId.systemDefault()))
                .build();
    }

    private String horaEstimada(EventoReserva evento) {
        Object hora = evento.payload().get("horaEstimada");
        return hora instanceof LocalDateTime h ? h.format(FORMATO_HORA) : "por confirmar";
    }

    private String motivo(EventoReserva evento) {
        Object motivo = evento.payload().get("motivo");
        Object comentario = evento.payload().get("comentario");
        String texto = motivo == null ? "sin especificar" : motivo.toString();
        return comentario == null ? texto : texto + " (" + comentario + ")";
    }
}