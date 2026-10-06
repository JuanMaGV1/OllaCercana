package com.ollacercana.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.ollacercana.model.dto.request.LoginRequestDTO;
import com.ollacercana.model.dto.response.ErrorResponseDTO;
import com.ollacercana.model.dto.response.LoginResponseDTO;

@Tag(name = "Sesiones", description = "API para gestión de sesiones y autenticación de usuarios")
public interface SesionApi {

    @Operation(
            summary = "Iniciar sesión de usuario",
            description = "Autentica un usuario mediante su correo o celular y contraseña, retornando sus datos básicos, roles y el token de acceso JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Petición inválida o error en los campos de entrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas o cuenta bloqueada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    ResponseEntity<LoginResponseDTO> iniciarSesion(@Valid @RequestBody LoginRequestDTO request);
}