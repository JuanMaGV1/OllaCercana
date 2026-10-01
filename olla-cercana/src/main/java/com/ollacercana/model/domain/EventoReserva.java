package com.ollacercana.model.domain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Evento de dominio que se publica cuando cambia una reserva (patrón Observer).
 * El dominio no sabe quién lo consume: notificaciones in-app, push, WebSocket, etc.
 * NO se persiste: es un mensaje efímero entre capas.
 */
public record EventoReserva(
        UUID id,
        TipoEvento tipo,
        UUID reservaId,
        UUID platoId,
        Long compradorId,
        UUID cocineraId,
        LocalDateTime timestamp,
        Map<String, Object> payload
) {

    public EventoReserva {
        payload = payload == null
                ? Map.of()
                : Collections.unmodifiableMap(new HashMap<>(payload));
    }

    public static EventoReserva de(TipoEvento tipo, Reserva reserva, Map<String, Object> payload) {
        return new EventoReserva(
                UUID.randomUUID(),
                tipo,
                reserva.getId(),
                reserva.getPlatoId(),
                reserva.getCompradorId(),
                reserva.getCocineraId(),
                LocalDateTime.now(),
                payload
        );
    }
}