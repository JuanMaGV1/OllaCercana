package com.ollacercana.controller.dtos.request;

import com.ollacercana.core.models.enums.RestriccionAlimentaria;
import com.ollacercana.core.models.enums.TipoComida;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Parámetros de consulta para platos cercanos (HU-06 + HU-07)")
public class ConsultaPlatosRequest {

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    @Schema(example = "4.6533")
    private Double lat;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    @Schema(example = "-74.0836")
    private Double lng;

    @Min(value = 500, message = "El radio mínimo es 500 m")
    @Max(value = 2000, message = "El radio máximo es 2000 m")
    @Schema(example = "1000")
    private Integer radioMetros = 2000;

    @Schema(example = "ALMUERZO")
    private TipoComida tipoComida;

    @Schema(description = "Restricciones alimentarias a filtrar (parámetro repetible)")
    private List<RestriccionAlimentaria> restricciones;

    @Min(0)
    @Schema(example = "0")
    private Integer page = 0;

    @Min(1) @Max(50)
    @Schema(example = "20")
    private Integer size = 20;
}