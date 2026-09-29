package com.ollacercana.dto.response;

import com.ollacercana.domain.RestriccionAlimentaria;
import com.ollacercana.domain.TipoComida;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta optimizada para visualización de platos cercanos sin revelar ubicación exacta")
public class PlatoCercanoResponseDTO {

    @Schema(description = "Identificador único del plato", example = "d3b07384-d113-494e-9c9e-5b1234567890")
    private UUID id;

    @Schema(description = "Nombre del plato", example = "Bandeja Paisa Casera")
    private String nombre;

    @Schema(description = "URL de la fotografía del plato", example = "https://fotos.ollacercana.com/bandeja.jpg")
    private String fotoUrl;

    @Schema(description = "Tipo de comida", example = "ALMUERZO")
    private TipoComida tipoComida;

    @Schema(description = "Restricciones alimentarias")
    private List<RestriccionAlimentaria> restricciones;

    @Schema(description = "Precio por porción", example = "15000.00")
    private BigDecimal precioPorcion;

    @Schema(description = "Cantidad de porciones disponibles para reserva", example = "4")
    private Integer porcionesDisponibles;

    @Schema(description = "Nombre del conjunto residencial", example = "Torres del Parque")
    private String conjunto;

    @Schema(description = "Distancia aproximada en metros redondeada a múltiplos de 100m (RN-05)", example = "900")
    private Integer distanciaAproximada;

    @Schema(description = "Tiempo restante de vigencia del plato", example = "2h 45m")
    private String tiempoRestante;
}