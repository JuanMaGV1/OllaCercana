package com.ollacercana.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Métricas acumuladas de ventas caseras de una cocinera (HU-20)")
public class MetricasCocineraResponseDTO {

    @Schema(description = "Total de ingresos referenciales de las reservas COMPLETADAS", example = "120000.00")
    private BigDecimal ingresosReferenciales;

    @Schema(description = "Total de porciones entregadas", example = "8")
    private long porcionesEntregadas;

    @Schema(description = "Nombre del plato con más pedidos; vacío si la cocinera no tiene ventas", example = "Sancocho de Pollo")
    private String platoMasPedido;
}
