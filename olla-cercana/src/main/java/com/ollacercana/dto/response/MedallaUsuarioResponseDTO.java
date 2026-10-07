package com.ollacercana.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Medalla vigente de un usuario (HU-21)")
public class MedallaUsuarioResponseDTO {

    @Schema(description = "Código de la medalla", example = "VECINO_FIEL")
    private String codigo;

    @Schema(description = "Nombre de la medalla", example = "Vecino Fiel")
    private String nombre;

    @Schema(description = "Requisito para obtenerla")
    private String requisito;

    private LocalDateTime fechaOtorgada;

    @Schema(description = "Fecha de vencimiento; nula si la medalla no vence")
    private LocalDateTime vigenteHasta;
}
