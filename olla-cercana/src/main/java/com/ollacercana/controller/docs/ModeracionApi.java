package com.ollacercana.controller.docs;

import com.ollacercana.controller.dtos.request.EjecutarDecisionDTO;
import com.ollacercana.controller.dtos.response.ErrorResponseDTO;
import com.ollacercana.core.models.PerfilCocinera;
import com.ollacercana.core.models.Reporte;
import com.ollacercana.core.models.enums.EstadoReporte;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.ollacercana.config.OpenApiConfig;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

/**
 * OC-035 HU-19: API del panel de moderación para ADMIN.
 */
@Tag(name = "Moderación", description = "HU-19 — Resolución de reportes y reactivación de perfiles")
public interface ModeracionApi {

    @Operation(summary = "Listar reportes (filtro opcional por estado)",
           security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME))
    @GetMapping("/reportes")
    ResponseEntity<Page<Reporte>> listarReportes(
            @RequestParam(required = false) EstadoReporte estado,
            @PageableDefault(size = 20, sort = "fechaCreacion", direction = Sort.Direction.DESC)
            Pageable pageable);

    @Operation(summary = "Detalle de un reporte",
               security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME))
    @GetMapping("/reportes/{reporteId}")
    ResponseEntity<Reporte> detalleReporte(@PathVariable UUID reporteId);

    @Operation(summary = "Ejecutar decisión sobre un reporte (RN-23)",
               security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME))
    @PostMapping("/reportes/{reporteId}/decision")
    ResponseEntity<Void> ejecutarDecision(@PathVariable UUID reporteId,
                                          @Valid @RequestBody EjecutarDecisionDTO dto);

    @Operation(summary = "Listar perfiles pausados por baja reputación (RN-09)",
               security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME))
    @GetMapping("/perfiles/pausados")
    ResponseEntity<List<PerfilCocinera>> listarPerfilesPausados();

    @Operation(summary = "Reactivar un perfil pausado",
               security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME))
    @PostMapping("/perfiles/{cocineraId}/reactivar")
    ResponseEntity<Void> reactivarPerfil(@PathVariable UUID cocineraId,
                                         @RequestBody String justificacion);
}