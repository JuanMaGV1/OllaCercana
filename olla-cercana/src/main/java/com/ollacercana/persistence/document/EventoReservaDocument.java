// persistence/document/EventoReservaDocument.java
package com.ollacercana.persistence.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.ollacercana.core.models.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Document(collection = "eventos_reserva")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoReservaDocument {

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
}