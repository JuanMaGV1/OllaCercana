package com.ollacercana.controller.dtos.response;

import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.models.enums.TipoNotificacion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notificación in-app del usuario (HU-17)")
public class NotificacionResponseDTO {
    private String id;
    private String titulo;
    private String mensaje;
    private TipoNotificacion tipo;
    private Rol rolDestinatario;
    private boolean leida;
    private LocalDateTime fechaCreacion;
}