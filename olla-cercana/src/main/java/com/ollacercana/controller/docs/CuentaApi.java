package com.ollacercana.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.ollacercana.config.OpenApiConfig;
import com.ollacercana.controller.dtos.request.RegistroRequestDTO;
import com.ollacercana.controller.dtos.response.RegistroResponseDTO;

import java.util.Map;

/**
 * Documentación OpenAPI de Gestión de Cuentas.
 *
 * FEAT-04 — Autenticación / Perfil
 * HU-01   — Registro de cuenta
 * HU-17   — Preferencias de avisos
 *
 * El patrón de la interfaz separa documentación de implementación:
 * {@link com.ollacercana.controller.CuentaController} la implementa y solo
 * aporta lógica; aquí vive todo {@code @Tag}, {@code @Operation} y
 * {@code @ApiResponse}.
 *
 * @see OC-060 DTOs RegistroRequest + RegistroResponse
 * @see OC-065 Endpoint POST /cuentas
 * @see OC-094 Preferencia de avisos in-app
 */
@Tag(name = "Gestión de Cuentas", description = "API para el registro y administración de cuentas de usuario")
public interface CuentaApi {

    @Operation(
            summary = "Registrar una nueva cuenta",
            description = "Crea una nueva cuenta de usuario en el sistema previa validación de unicidad y encriptación de contraseña con BCrypt."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cuenta creada exitosamente",
                    content = @Content(schema = @Schema(implementation = RegistroResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Petición inválida o error en los datos de entrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflicto: El correo o número celular ya se encuentra registrado", content = @Content)
    })
    ResponseEntity<RegistroResponseDTO> registrar(@Valid @RequestBody RegistroRequestDTO request);

    @Operation(
        summary = "Activar o desactivar avisos in-app",
        security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
        )
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Preferencia actualizada"),
                @ApiResponse(responseCode = "401", description = "No autenticado"),
                @ApiResponse(responseCode = "403", description = "No puedes modificar otra cuenta")
        })
        ResponseEntity<Map<String, Boolean>> cambiarAvisos(Long id, boolean activos);
}