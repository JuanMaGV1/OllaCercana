package com.ollacercana.controller.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitud para registrar o actualizar el token push del dispositivo")
public record TokenDispositivoRequestDTO(
        @NotBlank(message = "El token del dispositivo no puede estar vacío")
        @Schema(description = "Token push FCM del dispositivo móvil", example = "fcm_token_sample_abc1234567890")
        String token
) {}