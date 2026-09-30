package com.ollacercana.observer;

import com.ollacercana.domain.EventoReserva;
import com.ollacercana.domain.Notificacion;
import com.ollacercana.domain.Rol;
import com.ollacercana.domain.TipoNotificacion;
import com.ollacercana.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Component
@RequiredArgsConstructor
public class NotificacionInAppObservador implements ObservadorReserva {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificacionRepository notificacionRepository;

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
            // HU-23: el cierre avisa a ambas partes e invita a calificar.
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

        notificaciones.forEach(notificacionRepository::save);
    }

    private String mensajeCierre(EventoReserva evento) {
        boolean automatica = Boolean.TRUE.equals(evento.payload().get("automatica"));
        String origen = automatica
                ? "Pasaron 24 horas desde la confirmación y la reserva se completó automáticamente. "
                : "Se confirmó la entrega y el pago. ";
        return origen + "El chat quedó en solo lectura. ¡Ya puedes calificar la experiencia!";
    }

    private Notificacion paraComprador(EventoReserva evento, TipoNotificacion tipo, String titulo, String mensaje) {
        return base(evento, tipo, titulo, mensaje)
                .rolDestinatario(Rol.COMPRADOR)
                .compradorId(evento.compradorId())
                .build();
    }

    private Notificacion paraCocinera(EventoReserva evento, TipoNotificacion tipo, String titulo, String mensaje) {
        return base(evento, tipo, titulo, mensaje)
                .rolDestinatario(Rol.COCINERA)
                .cocineraId(evento.cocineraId())
                .build();
    }

    private Notificacion.NotificacionBuilder base(EventoReserva evento, TipoNotificacion tipo, String titulo, String mensaje) {
        return Notificacion.builder()
                .reservaId(evento.reservaId())
                .tipo(tipo)
                .titulo(titulo)
                .mensaje(mensaje)
                .leida(false)
                .fechaCreacion(LocalDateTime.now());
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
