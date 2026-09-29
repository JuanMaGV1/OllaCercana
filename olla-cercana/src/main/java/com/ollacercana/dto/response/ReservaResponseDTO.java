package com.ollacercana.dto.response;

import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.MedioPago;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta con los datos de la reserva creada")
public class ReservaResponseDTO {

    @Schema(description = "ID de la reserva")
    private UUID id;

    @Schema(description = "Estado actual de la reserva", example = "PENDIENTE")
    private EstadoReserva estado;

    @Schema(description = "Monto total a pagar", example = "30000.00")
    private BigDecimal monto;

    @Schema(description = "Hora límite para confirmación de la cocinera (+10 min)")
    private LocalDateTime horaLimite;

    @Schema(description = "Nombre del plato reservado", example = "Bandeja Paisa")
    private String plato;

    @Schema(description = "Nombre del conjunto residencial", example = "Torres del Parque")
    private String conjunto;

    @Schema(description = "Cantidad de porciones reservadas", example = "2")
    private Integer cantidadPorciones;

    @Schema(description = "Medio de pago", example = "NEQUI")
    private MedioPago medioPago;
}