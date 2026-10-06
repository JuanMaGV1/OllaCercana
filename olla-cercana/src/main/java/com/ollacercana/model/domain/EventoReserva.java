package com.ollacercana.model.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class EventoReserva {
    private String id;
    private TipoEvento tipo;
    private UUID reservaId;
    private UUID platoId;
    private Long compradorId;
    private UUID cocineraId;
    private LocalDateTime timestamp;
    private Map<String, Object> payload;

    @Builder
    public EventoReserva(String id,
                         TipoEvento tipo,
                         UUID reservaId,
                         UUID platoId,
                         Long compradorId,
                         UUID cocineraId,
                         LocalDateTime timestamp,
                         Map<String, Object> payload) {
        this.id = (id != null) ? id : UUID.randomUUID().toString();
        this.tipo = tipo;
        this.reservaId = reservaId;
        this.platoId = platoId;
        this.compradorId = compradorId;
        this.cocineraId = cocineraId;
        this.timestamp = (timestamp != null) ? timestamp : LocalDateTime.now();
        this.payload = (payload != null) ? payload : Map.of();
    }

    public static EventoReserva de(TipoEvento tipo, Reserva reserva, Map<String, Object> payload) {
        return EventoReserva.builder()
                .id(UUID.randomUUID().toString())
                .tipo(tipo)
                .reservaId(reserva.getId())
                .platoId(reserva.getPlatoId())
                .compradorId(reserva.getCompradorId())
                .cocineraId(reserva.getCocineraId())
                .timestamp(LocalDateTime.now())
                .payload(payload)
                .build();
    }

    public TipoEvento tipo() { return tipo; }
    public UUID reservaId() { return reservaId; }
    public UUID platoId() { return platoId; }
    public Long compradorId() { return compradorId; }
    public UUID cocineraId() { return cocineraId; }
    public LocalDateTime timestamp() { return timestamp; }
    public Map<String, Object> payload() { return payload != null ? payload : Map.of(); }
}