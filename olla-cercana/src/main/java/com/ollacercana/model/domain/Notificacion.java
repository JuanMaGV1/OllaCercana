package com.ollacercana.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    private UUID id;
    private UUID reservaId;
    private Rol rolDestinatario;
    private UUID compradorId;
    private UUID cocineraId;
    private String titulo;
    private String mensaje;
    private TipoNotificacion tipo;
    private boolean leida;
    private LocalDateTime fechaCreacion;

    public void marcarLeida() {
        this.leida = true;
    }

    public void validarDestinatario() {
        if (compradorId == null && cocineraId == null) {
            throw new IllegalStateException("La notificación debe tener un destinatario");
        }
        if (compradorId != null && cocineraId != null) {
            throw new IllegalStateException("La notificación no puede tener ambos destinatarios");
        }
    }
}