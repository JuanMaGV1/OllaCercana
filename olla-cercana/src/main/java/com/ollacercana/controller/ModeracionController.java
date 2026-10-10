package com.ollacercana.controller;

import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.docs.ModeracionApi;
import com.ollacercana.controller.dtos.request.EjecutarDecisionDTO;
import com.ollacercana.core.models.PerfilCocinera;
import com.ollacercana.core.models.Reporte;
import com.ollacercana.core.models.enums.EstadoReporte;
import com.ollacercana.core.services.IModeracionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

/**
 * HU-19 / OC-035: implementación del panel de moderación (solo ADMIN).
 * RN-09, RN-22, RN-23 aplicadas en el servicio.
 *
 * Nota: las anotaciones @GetMapping/@PostMapping se repiten en la clase porque
 * Spring MVC resuelve los mappings por reflexión directa sobre el controller,
 * no por herencia desde la interfaz. La interfaz ModeracionApi conserva la
 * documentación Swagger (@Operation, @Tag).
 */
@RestController
@RequestMapping("/api/v1/admin/moderacion")
@RequiredArgsConstructor
public class ModeracionController implements ModeracionApi {

    private final IModeracionService moderacionService;
    private final UsuarioActual usuarioActual;

    @Override
    @GetMapping("/reportes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Reporte>> listarReportes(
            @RequestParam(required = false) EstadoReporte estado,
            @PageableDefault(size = 20, sort = "fechaCreacion", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(moderacionService.obtenerReportes(estado, pageable));
    }

    @Override
    @GetMapping("/reportes/{reporteId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Reporte> detalleReporte(@PathVariable UUID reporteId) {
        return ResponseEntity.ok(moderacionService.obtenerDetalle(reporteId));
    }

    @Override
    @PostMapping("/reportes/{reporteId}/decision")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> ejecutarDecision(
            @PathVariable UUID reporteId,
            @Valid @RequestBody EjecutarDecisionDTO dto) {
        moderacionService.resolver(reporteId, dto, usuarioActual.getCuentaId());
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/perfiles/pausados")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PerfilCocinera>> listarPerfilesPausados() {
        return ResponseEntity.ok(moderacionService.listarPerfilesPausados());
    }

    @Override
    @PostMapping("/perfiles/{cocineraId}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reactivarPerfil(
            @PathVariable UUID cocineraId,
            @RequestBody String justificacion) {
        moderacionService.reactivarPerfil(cocineraId, justificacion, usuarioActual.getCuentaId());
        return ResponseEntity.ok().build();
    }
}