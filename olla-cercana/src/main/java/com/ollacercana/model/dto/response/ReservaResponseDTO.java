package com.ollacercana.model.dto.response;

import com.ollacercana.model.domain.EstadoChat;
import com.ollacercana.model.domain.EstadoReserva;
import com.ollacercana.model.domain.MedioPago;
import com.ollacercana.model.domain.MotivoRechazo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Respuesta con los datos de una reserva")
public record ReservaResponseDTO(
        UUID id,
        UUID platoId,
        UUID cocineraId,
        UUID compradorId,
        Integer cantidadPorciones,
        BigDecimal montoTotal,
        MedioPago medioPago,
        EstadoReserva estado,
        String notaComprador,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaLimiteConfirmacion,
        LocalDateTime fechaDecision,
        LocalDateTime horaEstimadaEntrega,
        MotivoRechazo motivoRechazo,
        String comentarioRechazo,
        boolean chatHabilitado,
        EstadoChat estadoChat,
        LocalDateTime fechaCompletada,
        String comentarioCierre,
        boolean calificacionHabilitada
) {}