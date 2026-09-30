package com.ollacercana.model.dto.response;

import com.ollacercana.model.domain.Rol;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Estructura con la respuesta de sesión exitosa y datos del usuario")
public class LoginResponseDTO {

    @Schema(description = "Token de autenticación JWT (Mock temporal para Sprint 2)", example = "bearer-token-placeholder")
    private String token;

    @Schema(description = "Identificador único de la cuenta", example = "1")
    private Long id;

    @Schema(description = "Nombre completo del usuario", example = "Carlos Pérez")
    private String nombre;

    @Schema(description = "Correo electrónico registrado", example = "carlos@gmail.com")
    private String correo;

    @Schema(description = "Rol asignado a la cuenta", example = "COMPRADOR")
    private Rol rol;
}