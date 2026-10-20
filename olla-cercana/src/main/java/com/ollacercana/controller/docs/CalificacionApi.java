package com.ollacercana.controller.docs;

import com.ollacercana.config.OpenApiConfig;
import com.ollacercana.controller.dtos.request.CalificacionRequestDTO;
import com.ollacercana.controller.dtos.response.CalificacionResponseDTO;
import com.ollacercana.controller.dtos.response.ErrorResponseDTO;
import com.ollacercana.controller.dtos.response.PaginaResponseDTO;
import com.ollacercana.controller.dtos.response.ResumenCalificacionesDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Documentación OpenAPI de Calificaciones y Reputación.
 *
 * FEAT-08 — Confianza y reputación
 * HU-15   — Calificación del intercambio
 * HU-22   — Consulta pública de reputación
 * RN-19   — Ventana de 72 h
 * RN-20   — Publicación simultánea
 * RN-31   — Una calificación por reserva, solo el comprador
 *
 * Los endpoints de consulta ({@code GET /cocineras/{id}/calificaciones})
 * son públicos; el de creación requiere rol COMPRADOR.
 *
 * @see OC-192 Entidad Calificacion
 * @see OC-199 Endpoint POST /reservas/{id}/calificacion
 * @see OC-200 Pruebas unitarias de calificación
 * @see OC-203/204 Reputación pública
 * @see OC-205 Pruebas unitarias de reputación
 */

@Tag(name = "Calificaciones", description = "HU-15 / HU-22 — calificación y reputación")
public interface CalificacionApi {

    @Operation(summary = "Calificar una reserva completada (HU-15)",
               security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME))
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Calificación registrada (PENDIENTE)"),
        @ApiResponse(responseCode = "403", description = "Solo el comprador de la reserva puede calificar",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Reserva no existe"),
        @ApiResponse(responseCode = "409", description = "Reserva ya calificada"),
        @ApiResponse(responseCode = "422", description = "Solo reservas COMPLETADAS")
    })
    @PostMapping("/reservas/{reservaId}/calificacion")
    ResponseEntity<CalificacionResponseDTO> calificar(
            @PathVariable UUID reservaId,
            @Valid @RequestBody CalificacionRequestDTO request);

    @Operation(summary = "Listar reseñas publicadas de una cocinera (HU-22)")
    @GetMapping("/cocineras/{cocineraId}/calificaciones")
    ResponseEntity<PaginaResponseDTO<CalificacionResponseDTO>> listar(
            @PathVariable UUID cocineraId,
            @RequestParam(required = false) Integer estrellas,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @Operation(summary = "Resumen agregado de reputación (HU-22)")
    @GetMapping("/cocineras/{cocineraId}/calificaciones/resumen")
    ResponseEntity<ResumenCalificacionesDTO> resumen(@PathVariable UUID cocineraId);
}