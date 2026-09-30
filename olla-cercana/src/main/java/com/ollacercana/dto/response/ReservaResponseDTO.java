package com.ollacercana.dto.response;

import com.ollacercana.domain.EstadoChat;
import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.MedioPago;
import com.ollacercana.domain.MotivoRechazo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
