package com.ollacercana.controller;

import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.dtos.request.CalificacionRequestDTO;
import com.ollacercana.controller.dtos.response.CalificacionResponseDTO;
import com.ollacercana.controller.dtos.response.PaginaResponseDTO;
import com.ollacercana.controller.dtos.response.ResumenCalificacionesDTO;
import com.ollacercana.core.services.CalificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CalificacionController {

    private final CalificacionService calificacionService;
    private final UsuarioActual usuarioActual;

    /**
     * HU-31: el comprador califica una reserva completada.
     * El compradorId se obtiene del JWT, no del body.
     */
    @PostMapping("/reservas/{reservaId}/calificacion")
    @PreAuthorize("hasRole('COMPRADOR')")
    public ResponseEntity<CalificacionResponseDTO> calificar(
            @PathVariable UUID reservaId,
            @Valid @RequestBody CalificacionRequestDTO request) {

        Long compradorId = usuarioActual.getCuentaId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(calificacionService.calificar(reservaId, compradorId, request));
    }

    /**
     * Lista paginada de calificaciones de una cocinera (público).
     * `estrellas` es opcional para filtrar.
     */
    @GetMapping("/cocineras/{cocineraId}/calificaciones")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaginaResponseDTO<CalificacionResponseDTO>> listar(
            @PathVariable UUID cocineraId,
            @RequestParam(required = false) Integer estrellas,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(calificacionService.listarPorCocinera(cocineraId, estrellas, page, size));
    }

    /**
     * Resumen agregado de calificaciones de una cocinera (público).
     */
    @GetMapping("/cocineras/{cocineraId}/calificaciones/resumen")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ResumenCalificacionesDTO> resumen(@PathVariable UUID cocineraId) {
        return ResponseEntity.ok(calificacionService.obtenerResumen(cocineraId));
    }
}