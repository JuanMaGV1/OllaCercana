package com.ollacercana.controller.docs;

import com.ollacercana.model.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.model.dto.request.PlatoRequestDTO;
import com.ollacercana.model.dto.response.PlatoCercanoResponseDTO;
import com.ollacercana.model.dto.response.PlatoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Platos", description = "Gestión de ofertas de comida casera")
@RequestMapping("/api/v1/platos")
public interface PlatoApi {

    @Operation(summary = "Publicar un nuevo plato (HU-04)")
    @PostMapping
    ResponseEntity<PlatoResponseDTO> crear(
            @RequestHeader("X-Cocinera-Id") UUID cocineraId,
            @Valid @RequestBody PlatoRequestDTO request
    );

    @Operation(summary = "Consultar un plato por id")
    @GetMapping("/{id}")
    ResponseEntity<PlatoResponseDTO> obtenerPorId(@PathVariable UUID id);

    @Operation(summary = "Ajustar la disponibilidad de un plato (HU-24)")
    @PatchMapping("/{id}/disponibilidad")
    ResponseEntity<PlatoResponseDTO> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AjusteDisponibilidadRequest request
    );

    @Operation(summary = "Listar platos cercanos (RN-05)")
    @GetMapping("/cercanos")
    ResponseEntity<List<PlatoCercanoResponseDTO>> listarCercanos(
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud
    );

    @Operation(summary = "Eliminar un plato")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> eliminar(@PathVariable UUID id);
}