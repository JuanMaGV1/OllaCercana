package com.ollacercana.controller.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Calificación publicada (HU-31)")
public class CalificacionResponseDTO {

    private UUID id;
    private UUID reservaId;
    private UUID cocineraId;
    private Long compradorId;
    private Integer estrellas;
    private String comentario;
    private LocalDateTime fechaCreacion;
}