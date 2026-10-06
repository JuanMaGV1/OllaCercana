package com.ollacercana.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

import com.ollacercana.model.domain.MedioPago;

@Schema(description = "Solicitud para crear una reserva de porciones de un plato")
public record ReservaRequestDTO(

        @NotNull(message = "El ID del plato es obligatorio")
        @Schema(description = "ID del plato a reservar", example = "d3b07384-d113-494e-9c9e-5b1234567890")
        UUID platoId,

        @NotNull(message = "La cantidad de porciones es obligatoria")
        @Min(value = 1, message = "Debe reservar al menos 1 porción")
        @Schema(description = "Cantidad de porciones", example = "2")
        Integer cantidad,

        @NotNull(message = "El medio de pago es obligatorio")
        @Schema(description = "Medio de pago acordado", example = "NEQUI")
        MedioPago medioPago,

        @Size(max = 500, message = "La nota no puede exceder 500 caracteres")
        @Schema(description = "Nota o instrucciones adicionales del comprador", example = "Sin cebolla por favor")
        String nota
) {}