package com.ollacercana.model.dto.response;

import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.MedioPago;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.ollacercana.domain.EstadoChat;
import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.MedioPago;
import com.ollacercana.domain.MotivoRechazo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
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
@Schema(description = "Estado actual de una reserva")
public record ReservaResponseDTO(
        UUID id,
        UUID platoId,
        UUID cocineraId,
        Long compradorId,
        Integer cantidadPorciones,
        BigDecimal montoTotal,
        MedioPago medioPago,
        EstadoReserva estado,
        String notaComprador,
        LocalDateTime fechaCreacion,
        @Schema(description = "Hora límite para que la cocinera responda (RN-04)")
        LocalDateTime fechaLimiteConfirmacion,
        LocalDateTime fechaDecision,
        LocalDateTime horaEstimadaEntrega,
        MotivoRechazo motivoRechazo,
        String comentarioRechazo,
        @Schema(description = "true cuando la reserva fue confirmada y existe el chat de coordinación")
        boolean chatHabilitado,
        @Schema(description = "ACTIVO al confirmar; SOLO_LECTURA cuando la reserva se completa (RN-17)")
        EstadoChat estadoChat,
        @Schema(description = "Momento en que se cerró la transacción (HU-23)")
        LocalDateTime fechaCompletada,
        @Schema(description = "Comentario opcional del cierre (HU-23)")
        String comentarioCierre,
        @Schema(description = "true cuando la reserva está COMPLETADA y comprador y cocinera pueden calificarse")
        boolean calificacionHabilitada
) {}
