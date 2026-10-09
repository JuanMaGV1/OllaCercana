package com.ollacercana.controller.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Criterios para consultar cocineras con oferta activa en el mapa dentro de un radio")
public class MapaCocinerasRequestDTO {

    public static final double RADIO_MAXIMO_METROS = 50000.0;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe ser mayor o igual a -90")
    @DecimalMax(value = "90.0", message = "La latitud debe ser menor o igual a 90")
    @Schema(description = "Latitud del centro de consulta", example = "4.6789")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
    @DecimalMax(value = "180.0", message = "La longitud debe ser menor o igual a 180")
    @Schema(description = "Longitud del centro de consulta", example = "-74.0567")
    private Double longitud;

    @NotNull(message = "El radio de búsqueda es obligatorio")
    @Positive(message = "El radio de búsqueda debe ser mayor a 0")
    @DecimalMax(value = "50000.0", message = "El radio de búsqueda no puede superar los 50000 metros")
    @Schema(description = "Radio de búsqueda en metros (máximo 50000)", example = "3000")
    private Double radio;
}