package com.ollacercana.controller.dtos.response;

import com.ollacercana.core.models.enums.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalle del mensaje en el chat")
public class MensajeResponseDTO {

    private String id;
    private String autor;
    private Rol autorRol;
    private String autorId;
    private String texto;
    private LocalDateTime fecha;
    private boolean leido;
}