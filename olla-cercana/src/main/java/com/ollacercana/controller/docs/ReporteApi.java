package com.ollacercana.controller.docs;

import com.ollacercana.config.OpenApiConfig;
import com.ollacercana.controller.dtos.request.ReporteCrearDTO;
import com.ollacercana.controller.dtos.response.ErrorResponseDTO;
import com.ollacercana.controller.dtos.response.ReporteDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * HU-18 / OC-034: reportar publicaciones o usuarios por incumplimiento.
 * RN-22 (ocultamiento preventivo con 3 reportantes distintos),
 * RN-23 (toda decisión de moderación queda auditada).
 *
 * Los motivos del reporte se exponen para alimentar el combo del formulario.
 */
@Tag(name = "Reportes", description = "HU-18 — Reportar publicaciones o usuarios")
@RequestMapping("/api/v1/reportes")
public interface ReporteApi {

    @Operation(
            summary = "Crear un reporte (HU-18)",
            description = "Crea un reporte sobre un plato o una cuenta. Requiere rol COMPRADOR o COCINERA.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reporte creado en estado ABIERTO",
                    content = @Content(schema = @Schema(implementation = ReporteDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado (requiere COMPRADOR o COCINERA)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regla de negocio violada (autorreporte, duplicado, sin motivo)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping
    ResponseEntity<ReporteDTO> crearReporte(@Valid @RequestBody ReporteCrearDTO dto);

    @Operation(
            summary = "Listar los motivos de reporte disponibles",
            description = "Alimenta el selector del formulario de reporte en el frontend."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de motivos",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
    })
    @GetMapping("/motivos")
    ResponseEntity<List<String>> obtenerMotivos();
}