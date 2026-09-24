package com.ollacercana.controller;

import com.ollacercana.domain.TipoComida;
import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.dto.response.PlatoResponseDTO;
import com.ollacercana.service.PlatoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/platos")
@RequiredArgsConstructor
@Tag(name = "Platos", description = "Gestión de platos caseros (OC-004, OC-005, OC-006)")
public class PlatoController {

    private final PlatoService platoService;

    @PostMapping
    @Operation(summary = "Publicar un plato casero")
    public ResponseEntity<PlatoResponseDTO> publicar(@Valid @RequestBody PlatoRequestDTO request) {
        PlatoResponseDTO response = platoService.publicar(request);
        return ResponseEntity
            .created(URI.create("/api/v1/platos/" + response.id()))
            .body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un plato por ID")
    public ResponseEntity<PlatoResponseDTO> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(platoService.obtenerPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar platos activos y vigentes")
    public ResponseEntity<List<PlatoResponseDTO>> listarActivos() {
        return ResponseEntity.ok(platoService.listarActivos());
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Listar platos por tipo de comida")
    public ResponseEntity<List<PlatoResponseDTO>> listarPorTipo(@PathVariable TipoComida tipo) {
        return ResponseEntity.ok(platoService.listarPorTipo(tipo));
    }

    @PatchMapping("/{id}/porciones")
    @Operation(summary = "Ajustar las porciones de un plato")
    public ResponseEntity<PlatoResponseDTO> ajustarPorciones(
        @PathVariable UUID id,
        @RequestParam int cantidad
    ) {
        return ResponseEntity.ok(platoService.ajustarPorciones(id, cantidad));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un plato")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        platoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}