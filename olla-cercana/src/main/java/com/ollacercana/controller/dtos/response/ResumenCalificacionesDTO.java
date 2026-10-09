package com.ollacercana.controller.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resumen agregado de calificaciones de una cocinera")
public class ResumenCalificacionesDTO {

    @Schema(description = "Promedio de estrellas (null si no hay calificaciones)")
    private Double promedio;

    @Schema(description = "Total de calificaciones registradas")
    private Long total;

    @Schema(description = "Cantidad de reseñas con 4 o 5 estrellas")
    private Long positivas;
}