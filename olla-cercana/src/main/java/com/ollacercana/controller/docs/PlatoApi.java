package com.ollacercana.controller.docs;

import com.ollacercana.config.OpenApiConfig;
import com.ollacercana.controller.dtos.request.AjusteDisponibilidadRequest;
import com.ollacercana.controller.dtos.request.ConsultaPlatosRequest;
import com.ollacercana.controller.dtos.request.PlatoRequestDTO;
import com.ollacercana.controller.dtos.response.PaginaResponseDTO;
import com.ollacercana.controller.dtos.response.PlatoCercanoResponseDTO;
import com.ollacercana.controller.dtos.response.PlatoResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Documentación OpenAPI de Platos.
 *
 * FEAT-05 — Oferta (administración)
 * FEAT-06 — Catálogo (consulta)
 * HU-04   — Publicar oferta
 * HU-05   — Porciones y precio
 * HU-06   — Platos cercanos por radio (RN-05)
 * HU-07   — Filtros por tipo y restricciones
 * HU-24   — Actualizar disponibilidad
 *
 * Nota: los endpoints de consulta pública ({@code GET /platos/cercanos}
 * y {@code GET /platos/{id}}) no exponen {@code puntoEntrega} (RN-05).
 *
 * @see OC-088 DTOs PlatoRequest + PlatoResponse
 * @see OC-093 Endpoints POST/GET/PUT/DELETE /platos
 * @see OC-109 Endpoint PATCH /disponibilidad
 * @see OC-114 DTO ConsultaPlatosRequest
 * @see OC-117 Endpoint GET /platos/cercanos
 * @see OC-121 DTO PlatoCercanoResponse
 * @see OC-123 Nunca exponer dirección exacta (RN-05)
 * @see OC-127 Filtro por tipoComida
 * @see OC-128 Filtro por restricciones alimentarias
 */
@Tag(name = "Platos", description = "Gestión de ofertas de comida casera")
@RequestMapping("/api/v1/platos")
public interface PlatoApi {

    @Operation(
            summary = "Publicar un nuevo plato (HU-04)",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plato publicado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (Requiere rol COCINERA)"),
            @ApiResponse(responseCode = "404", description = "Cocinera no encontrada"),
            @ApiResponse(responseCode = "409", description = "Límite de 3 platos activos o cocinera no habilitada"),
            @ApiResponse(responseCode = "422", description = "Precio, porciones o restricciones fuera de rango")
    })
    @PostMapping
    ResponseEntity<PlatoResponseDTO> crear(@Valid @RequestBody PlatoRequestDTO request);

    @Operation(summary = "Consultar un plato por id")
    @GetMapping("/{id}")
    ResponseEntity<PlatoResponseDTO> obtenerPorId(@PathVariable UUID id);

    @Operation(
            summary = "Ajustar la disponibilidad de un plato (HU-24)",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @PatchMapping("/{id}/disponibilidad")
    ResponseEntity<PlatoResponseDTO> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AjusteDisponibilidadRequest request
    );

       @Operation(
            summary = "Consultar platos cercanos (HU-06 + HU-07, RN-05)",
            description = """
                Retorna los platos disponibles dentro del radio especificado, ordenados por cercanÃ­a.
                No revela la direcciÃ³n exacta de la cocinera (RN-05).
                Filtros opcionales: tipoComida (HU-07) y restricciones alimentarias (HU-07).
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado paginado de platos cercanos"),
            @ApiResponse(responseCode = "400", description = "Latitud/longitud faltantes o fuera de rango")
    })
    @GetMapping("/cercanos")
    ResponseEntity<PaginaResponseDTO<PlatoCercanoResponseDTO>> consultarCercanos(
            @ParameterObject @Valid @ModelAttribute ConsultaPlatosRequest request
    );

    @Operation(
            summary = "Eliminar un plato",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @DeleteMapping("/{id}")
    ResponseEntity<Void> eliminar(@PathVariable UUID id);

    
}