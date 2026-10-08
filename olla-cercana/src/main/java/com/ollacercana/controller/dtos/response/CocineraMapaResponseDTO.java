package com.ollacercana.controller.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta de cocinera con oferta activa para el mapa (OC-233)")
public class CocineraMapaResponseDTO {

    @Schema(description = "ID del perfil de la cocinera")
    private UUID cocineraId;

    @Schema(description = "Nombre de la cocinera o conjunto residencial")
    private String nombreCocinera;

    @Schema(description = "Nombre del plato activo")
    private String platoNombre;

    @Schema(description = "Fotografía del plato")
    private String fotoPlato;

    @Schema(description = "Precio del plato")
    private BigDecimal precio;

    @Schema(description = "Latitud ofuscada con margen de seguridad (OC-235)")
    private Double latitud;

    @Schema(description = "Longitud ofuscada con margen de seguridad (OC-235)")
    private Double longitud;

    @Schema(description = "Distancia estimada en metros redondeada a múltiplos de 100m")
    private Integer distanciaMetros;
}