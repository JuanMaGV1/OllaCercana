package com.ollacercana.controller.docs;

import com.ollacercana.config.OpenApiConfig;
import com.ollacercana.controller.dtos.request.TokenDispositivoRequestDTO;
import com.ollacercana.controller.dtos.response.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Tag(name = "Dispositivos", description = "Registro de tokens para notificaciones push móviles (OC-273)")
@RequestMapping("/api/v1/dispositivos")
public interface DispositivoApi {

    @Operation(
            summary = "Registrar token push del dispositivo móvil (OC-273)",
            description = "Asocia el token push del dispositivo a la cuenta autenticada para el despacho de alertas.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Token inválido o vacío", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/token")
    ResponseEntity<Map<String, String>> registrarToken(@Valid @RequestBody TokenDispositivoRequestDTO request);
}