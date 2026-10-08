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
@Schema(description = "Página del historial de pedidos de la cocinera, del más reciente al más antiguo")
public class HistorialPaginadoResponseDTO {

    private List<HistorialPedidoResponseDTO> contenido;
    private int pagina;
    private int tamanio;
    private long totalElementos;
    private int totalPaginas;
}