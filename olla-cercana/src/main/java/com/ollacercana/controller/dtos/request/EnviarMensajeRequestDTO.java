package com.ollacercana.controller.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud para enviar un mensaje en el chat de una reserva")
public record EnviarMensajeRequestDTO(

        @NotBlank(message = "El texto del mensaje no puede estar vacío")
        @Size(max = 500, message = "El texto no puede superar los 500 caracteres")
        @Schema(description = "Contenido del mensaje (máx. 500 caracteres)", example = "Hola, ya voy en camino con tu pedido.")
        String texto
) {}
