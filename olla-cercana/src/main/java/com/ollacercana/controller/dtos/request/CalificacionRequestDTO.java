package com.ollacercana.controller.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para calificar una reserva completada (HU-31)")
public class CalificacionRequestDTO {

    @NotNull(message = "Debe indicar las estrellas (1-5)")
    @Min(value = 1, message = "El mínimo es 1 estrella")
    @Max(value = 5, message = "El máximo es 5 estrellas")
    @Schema(description = "Estrellas otorgadas", example = "5")
    private Integer estrellas;

    @Size(max = 500, message = "El comentario no puede superar 500 caracteres")
    @Schema(description = "Comentario opcional", example = "Excelente comida")
    private String comentario;
}