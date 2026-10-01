package com.ollacercana.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Estructura para la solicitud de inicio de sesión")
public class LoginRequestDTO {

    @NotBlank(message = "El identificador (correo o celular) es obligatorio")
    @Schema(description = "Correo electrónico o número de celular", example = "carlos@gmail.com")
    private String identificador;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña en texto plano", example = "Password123")
    private String contrasena;
}