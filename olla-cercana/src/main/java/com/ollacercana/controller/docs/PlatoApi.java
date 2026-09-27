package com.ollacercana.controller.docs;

import com.ollacercana.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.dto.response.PlatoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Platos", description = "Gestión de ofertas de comida casera")
@RequestMapping("/api/v1/platos")
public interface PlatoApi {

    @Operation(summary = "Publicar un nuevo plato (HU-04)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plato publicado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (validación de formulario)"),
            @ApiResponse(responseCode = "404", description = "Cocinera no encontrada"),
            @ApiResponse(responseCode = "409", description = "Límite de 3 platos activos, cocinera no verificada o pausada"),
            @ApiResponse(responseCode = "422", description = "Precio, porciones o restricciones fuera de los rangos permitidos")
    })
    @PostMapping
    ResponseEntity<PlatoResponseDTO> crear(
            @RequestHeader("X-Cocinera-Id") UUID cocineraId,
            @Valid @RequestBody PlatoRequestDTO request
    );

    @Operation(summary = "Consultar un plato por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plato encontrado"),
            @ApiResponse(responseCode = "404", description = "Plato no encontrado")
    })
    @GetMapping("/{id}")
    ResponseEntity<PlatoResponseDTO> obtenerPorId(@PathVariable UUID id);

    @Operation(summary = "Ajustar la disponibilidad de un plato (HU-24)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plato actualizado"),
            @ApiResponse(responseCode = "404", description = "Plato no encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflicto de versión (edición concurrente)"),
            @ApiResponse(responseCode = "422", description = "Cantidad inválida o reducción por debajo de comprometidas")
    })
    @PatchMapping("/{id}/disponibilidad")
    ResponseEntity<PlatoResponseDTO> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AjusteDisponibilidadRequest request
    );

    @Operation(summary = "Eliminar un plato")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Plato eliminado"),
            @ApiResponse(responseCode = "404", description = "Plato no encontrado")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> eliminar(@PathVariable UUID id);
}