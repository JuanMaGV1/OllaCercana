package com.ollacercana.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "eventos_reserva")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEvento tipo;

    @Column(nullable = false)
    private UUID reservaId;

    @Column(nullable = false)
    private UUID platoId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String payload;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Evento de dominio que se publica cuando cambia una reserva (patrón Observer).
 * El dominio no sabe quién lo consume: notificaciones in-app, push, WebSocket, etc.
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