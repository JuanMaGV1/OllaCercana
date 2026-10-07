package com.ollacercana.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Detalle de un pedido completado en el historial de la cocinera (HU-20)")
public class HistorialPedidoResponseDTO {

    private UUID reservaId;
    private String plato;
    private LocalDateTime fecha;
    private String comprador;
    private Integer porciones;

    @Schema(description = "Calificación obtenida (1-5); nula si el comprador aún no califica")
    private Integer calificacion;
}
