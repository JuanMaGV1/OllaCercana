package com.ollacercana.controller.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta paginada genérica")
public class PaginaResponseDTO<T> {

    @Schema(description = "Elementos de la página")
    private List<T> contenido;

    @Schema(description = "Número de página actual (0-based)", example = "0")
    private int page;

    @Schema(description = "Tamaño de página", example = "20")
    private int size;

    @Schema(description = "Total de elementos", example = "42")
    private long totalElementos;

    @Schema(description = "Total de páginas", example = "3")
    private int totalPaginas;

    @Schema(description = "Si hay más páginas", example = "true")
    private boolean hayMas;
}