package com.ollacercana.persistence.document;

import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.models.enums.TipoNotificacion;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "notificaciones")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificacionDocument {

    @Id private String id;
    private UUID reservaId;
    private Rol rolDestinatario;
    private Long compradorId;
    private UUID cocineraId;
    private String titulo;
    private String mensaje;
    private TipoNotificacion tipo;
    private boolean leida;
    private LocalDateTime fechaCreacion;
}