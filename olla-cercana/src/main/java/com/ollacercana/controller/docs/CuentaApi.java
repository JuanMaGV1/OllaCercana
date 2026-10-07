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

import com.ollacercana.controller.dtos.request.RegistroRequestDTO;
import com.ollacercana.controller.dtos.response.RegistroResponseDTO;

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
}