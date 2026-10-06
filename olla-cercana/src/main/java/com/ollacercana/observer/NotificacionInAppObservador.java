package com.ollacercana.observer;

import com.ollacercana.mapper.NotificacionDocumentMapper;
import com.ollacercana.model.domain.EventoReserva;
import com.ollacercana.model.domain.Notificacion;
import com.ollacercana.model.domain.Rol;
import com.ollacercana.model.domain.TipoNotificacion;
import com.ollacercana.repository.mongo.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionInAppObservador implements ObservadorReserva {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificacionRepository notificacionRepository;
    private final NotificacionDocumentMapper notificacionMapper;

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
            case RECORDATORIO_RESERVA -> List.of(paraCocinera(evento, TipoNotificacion.RECORDATORIO,
                    "Tienes una solicitud sin responder",
                    "Te quedan " + evento.payload().getOrDefault("minutosRestantes", "pocos")
                            + " minutos para confirmar o rechazar la reserva. Si no respondes, expirará automáticamente."));
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
                notificaciones.forEach(n -> notificacionRepository.save(notificacionMapper.toDocument(n)));
            } catch (Exception e) {
                log.warn("No se pudo persistir la notificación en Mongo: {}", e.getMessage());
            }
        }
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
                .fechaCreacion(LocalDateTime.now())
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