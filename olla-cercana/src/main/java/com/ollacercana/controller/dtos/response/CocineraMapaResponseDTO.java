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
@Schema(description = "Datos de una cocinera con oferta activa para el mapa interactivo")
public class CocineraMapaResponseDTO {

    @Schema(description = "ID del perfil de la cocinera", example = "11111111-1111-1111-1111-111111111111")
    private UUID cocineraId;

    @Schema(description = "Nombre de la cocinera o cocina", example = "Doña Rosalba")
    private String nombreCocinera;

    @Schema(description = "Latitud ofuscada para proteger privacidad", example = "4.6795")
    private Double latitudOfuscada;

    @Schema(description = "Longitud ofuscada para proteger privacidad", example = "-74.0572")
    private Double longitudOfuscada;

    @Schema(description = "URL de la foto del plato representativo", example = "https://fotos.ollacercana.com/ajiaco.jpg")
    private String fotoPlato;

    @Schema(description = "Nombre del plato activo", example = "Ajiaco santafereño")
    private String nombrePlato;

    @Schema(description = "Precio por porción del plato", example = "16000.00")
    private BigDecimal precio;

    @Schema(description = "Distancia estimada calculada antes de ofuscar y redondeada a múltiplos de 100m", example = "400")
    private Integer distanciaMetros;
}
