package com.ollacercana.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "notificaciones")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    private String id;

    private UUID reservaId;
    private Rol rolDestinatario;
    private Long compradorId;
    private UUID cocineraId;
    private String titulo;
    private String mensaje;
    private TipoNotificacion tipo;
    private boolean leida;
    private LocalDateTime fechaCreacion;

    public void marcarLeida() {
        this.leida = true;
    }
}