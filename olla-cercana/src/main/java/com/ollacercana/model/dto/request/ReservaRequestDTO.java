package com.ollacercana.model.dto.request;

import com.ollacercana.model.domain.MedioPago;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Solicitud para crear una reserva")
public record ReservaRequestDTO(

        @NotNull(message = "El ID del plato es obligatorio")
        UUID platoId,

        @NotNull(message = "La cantidad de porciones es obligatoria")
        @Min(value = 1, message = "Debe reservar al menos 1 porción")
        Integer cantidad,

        @NotNull(message = "El medio de pago es obligatorio")
        MedioPago medioPago,

        @Size(max = 300, message = "La nota no puede exceder 300 caracteres")
        String nota
) {}