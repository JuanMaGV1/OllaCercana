package com.ollacercana.controller.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.ollacercana.core.models.enums.Rol;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Estructura con la respuesta de sesión exitosa y datos del usuario")
public class LoginResponseDTO {

    @Schema(description = "Token de autenticación JWT firmado", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Identificador único de la cuenta", example = "1")
    private Long id;

    @Schema(description = "Nombre completo del usuario", example = "Carlos Pérez")
    private String nombre;

    @Schema(description = "Correo electrónico registrado", example = "carlos@gmail.com")
    private String correo;

    @Schema(description = "Rol principal de la cuenta", example = "COMPRADOR")
    private Rol rol;

    @Schema(description = "Lista completa de roles asignados a la cuenta")
    private List<String> roles;
}