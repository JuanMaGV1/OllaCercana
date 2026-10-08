package com.ollacercana.controller;

import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.dtos.request.EjecutarDecisionDTO;
import com.ollacercana.core.models.PerfilCocinera;
import com.ollacercana.core.models.Reporte;
import com.ollacercana.core.models.enums.EstadoReporte;
import com.ollacercana.core.services.ModeracionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/moderacion")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ModeracionApi {

    private final ModeracionService moderacionService;
    private final UsuarioActual usuarioActual;

    @GetMapping("/reportes")
    public ResponseEntity<Page<Reporte>> listarReportes(
            @RequestParam(required = false) EstadoReporte estado,
            Pageable pageable) {
        return ResponseEntity.ok(moderacionService.obtenerReportes(estado, pageable));
    }

    @GetMapping("/reportes/{reporteId}")
    public ResponseEntity<Reporte> detalleReporte(@PathVariable UUID reporteId) {
        return ResponseEntity.ok(moderacionService.obtenerDetalle(reporteId));
    }

    @PostMapping("/reportes/{reporteId}/decision")
    public ResponseEntity<Void> ejecutarDecision(
            @PathVariable UUID reporteId,
            @Valid @RequestBody EjecutarDecisionDTO dto) {
        moderacionService.resolver(reporteId, dto, usuarioActual.getCuentaId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/perfiles/pausados")
    public ResponseEntity<List<PerfilCocinera>> listarPerfilesPausados() {
        return ResponseEntity.ok(moderacionService.listarPerfilesPausados());
    }

    @PostMapping("/perfiles/{cocineraId}/reactivar")
    public ResponseEntity<Void> reactivarPerfil(
            @PathVariable UUID cocineraId,
            @RequestBody String justificacion) {
        moderacionService.reactivarPerfil(cocineraId, justificacion, usuarioActual.getCuentaId());
        return ResponseEntity.ok().build();
    }
}