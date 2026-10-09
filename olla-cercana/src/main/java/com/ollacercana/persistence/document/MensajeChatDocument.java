package com.ollacercana.persistence.document;

import com.ollacercana.core.models.enums.Rol;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "mensajes_chat")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajeChatDocument {

    @Id
    private String id;
    private UUID reservaId;
    private Rol autorRol;
    private String autorId;
    private String texto;
    private LocalDateTime fecha;
    private boolean leido;
}