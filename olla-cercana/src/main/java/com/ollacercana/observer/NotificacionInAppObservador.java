package com.ollacercana.observer;

import com.ollacercana.mapper.NotificacionEntityMapper;
import com.ollacercana.model.domain.EventoReserva;
import com.ollacercana.model.domain.Notificacion;
import com.ollacercana.model.domain.Rol;
import com.ollacercana.model.domain.TipoNotificacion;
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
    private final NotificacionEntityMapper notificacionMapper;

    @Override
    public void notificar(EventoReserva evento) {
        List<Notificacion> notificaciones = switch (evento.tipo()) {
            case RESERVA_CONFIRMADA -> List.of(paraComprador(evento, TipoNotificacion.RESERVA_CONFIRMADA,
                    "¡Tu reserva fue confirmada!",
                    "La cocinera confirmó tu pedido. Hora estimada: " + horaEstimada(evento)));
            case RESERVA_RECHAZADA -> List.of(paraComprador(evento, TipoNotificacion.RESERVA_RECHAZADA,
                    "Tu reserva fue rechazada",
                    "Motivo: " + motivo(evento)));
            case RESERVA_EXPIRADA -> List.of(paraComprador(evento, TipoNotificacion.RESERVA_EXPIRADA,
                    "Tu reserva expiró",
                    "La cocinera no respondió a tiempo."));
            case RECORDATORIO_RESERVA -> List.of(paraCocinera(evento, TipoNotificacion.RECORDATORIO,
                    "Solicitud sin responder",
                    "Te quedan " + evento.payload().getOrDefault("minutosRestantes", "pocos") + " minutos."));
            case RESERVA_COMPLETADA -> {
                boolean automatica = Boolean.TRUE.equals(evento.payload().get("automatica"));
                String msg = automatica
                        ? "Pasaron 24h desde la confirmación. La reserva se completó automáticamente."
                        : "Se confirmó la entrega. ¡Ya puedes calificar!";
                yield List.of(
                        paraComprador(evento, TipoNotificacion.INVITACION_CALIFICAR, "Pedido completado", msg),
                        paraCocinera(evento, TipoNotificacion.INVITACION_CALIFICAR, "Pedido completado", msg)
                );
            }
            default -> List.of();
        };

        notificaciones.forEach(n -> notificacionRepository.save(notificacionMapper.toEntity(n)));
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

    private Notificacion.NotificacionBuilder base(EventoReserva evento, TipoNotificacion tipo,
                                                   String titulo, String mensaje) {
        return Notificacion.builder()
                .id(java.util.UUID.randomUUID())
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