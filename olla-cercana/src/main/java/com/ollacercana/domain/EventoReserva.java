package com.ollacercana.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

    private Long compradorId;

    private UUID cocineraId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Transient
    private Map<String, Object> payload;

    @Column(columnDefinition = "TEXT")
    private String payloadJson;

    public EventoReserva(UUID id, TipoEvento tipo, UUID reservaId, UUID platoId,
                         Long compradorId, UUID cocineraId, LocalDateTime timestamp,
                         Map<String, Object> payload) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tipo = tipo;
        this.reservaId = reservaId;
        this.platoId = platoId;
        this.compradorId = compradorId;
        this.cocineraId = cocineraId;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.payload = payload == null ? Map.of() : Collections.unmodifiableMap(new HashMap<>(payload));
        this.payloadJson = this.payload.toString();
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

    // Métodos accesores para compatibilidad con el observador
    public TipoEvento tipo() { return this.tipo; }
    public UUID reservaId() { return this.reservaId; }
    public UUID platoId() { return this.platoId; }
    public Long compradorId() { return this.compradorId; }
    public UUID cocineraId() { return this.cocineraId; }
    public LocalDateTime timestamp() { return this.timestamp; }
    public Map<String, Object> payload() {
        return this.payload != null ? this.payload : Map.of();
    }
}