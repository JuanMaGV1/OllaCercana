package com.ollacercana.controller.docs;

import com.ollacercana.config.OpenApiConfig;
import com.ollacercana.controller.dtos.response.ErrorResponseDTO;
import com.ollacercana.controller.dtos.response.NotificacionResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notificaciones", description = "Historial de avisos in-app del usuario (HU-17)")
@RequestMapping("/api/v1/notificaciones")
public interface NotificacionApi {

    @Operation(
            summary = "Listar notificaciones del usuario autenticado",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de notificaciones",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificacionResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    ResponseEntity<List<NotificacionResponseDTO>> listar();

    @Operation(
            summary = "Marcar notificación como leída",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notificación marcada como leída"),
            @ApiResponse(responseCode = "403", description = "La notificación no te pertenece"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    @PatchMapping("/{id}/leida")
    ResponseEntity<Void> marcarLeida(@PathVariable String id);

    @Operation(
            summary = "Contar notificaciones no leídas del usuario",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conteo de no leídas"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/no-leidas/conteo")
    ResponseEntity<Long> contarNoLeidas();
}