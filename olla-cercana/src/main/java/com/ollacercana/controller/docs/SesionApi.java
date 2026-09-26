package com.ollacercana.controller.docs;

import com.ollacercana.dto.request.LoginRequestDTO;
import com.ollacercana.dto.response.ErrorResponseDTO;
import com.ollacercana.dto.response.LoginResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Sesiones", description = "API para gestión de sesiones y autenticación de usuarios")
public interface SesionApi {

    @Operation(
            summary = "Iniciar sesión de usuario",
            description = "Autentica un usuario mediante su correo o celular y contraseña, retornando sus datos básicos y el token de acceso."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Petición inválida o error en los datos de entrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Credenciales inválidas (correo/celular o contraseña erróneos)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    ResponseEntity<LoginResponseDTO> iniciarSesion(@Valid @RequestBody LoginRequestDTO request);
}