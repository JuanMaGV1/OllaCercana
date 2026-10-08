package com.ollacercana.core.models;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.models.enums.TipoNotificacion;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Notificacion {

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

    public void marcarLeida() { this.leida = true; }
}