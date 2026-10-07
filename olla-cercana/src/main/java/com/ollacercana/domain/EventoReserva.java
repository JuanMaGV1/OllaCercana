package com.ollacercana.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Document(collection = "eventos_reserva")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoReserva {

    @Id
    private String id;

    private TipoEvento tipo;
    private UUID reservaId;
    private UUID platoId;
    private Long compradorId;
    private UUID cocineraId;
    private LocalDateTime timestamp;
    private Map<String, Object> payload;
    private String payloadJson;

                                                                                  
    public EventoReserva(UUID id, TipoEvento tipo, UUID reservaId, UUID platoId,
                         Long compradorId, UUID cocineraId, LocalDateTime timestamp,
                         Map<String, Object> payload) {
        this.id = id != null ? id.toString() : UUID.randomUUID().toString();
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
        Map<String, Object> mapaPayload = payload != null ? payload : Map.of();
        return EventoReserva.builder()
                .id(UUID.randomUUID().toString())
                .tipo(tipo)
                .reservaId(reserva.getId())
                .platoId(reserva.getPlatoId())
                .compradorId(reserva.getCompradorId())
                .cocineraId(reserva.getCocineraId())
                .timestamp(LocalDateTime.now())
                .payload(mapaPayload)
                .payloadJson(mapaPayload.toString())
                .build();
    }

                                                                      
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