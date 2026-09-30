package com.ollacercana.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ollacercana.model.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.model.dto.request.PlatoRequestDTO;
import com.ollacercana.model.dto.response.PlatoCercanoResponseDTO;
import com.ollacercana.model.dto.response.PlatoResponseDTO;

import java.util.List;
import java.util.UUID;

@Tag(name = "Platos", description = "Gestión de ofertas de comida casera")
@RequestMapping("/api/v1/platos")
public interface PlatoApi {

    @Operation(summary = "Publicar un nuevo plato (HU-04)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plato publicado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Cocinera no encontrada"),
            @ApiResponse(responseCode = "409", description = "Límite de 3 platos activos o cocinera no habilitada"),
            @ApiResponse(responseCode = "422", description = "Precio, porciones o restricciones fuera de rango")
    })
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

    @Operation(
            summary = "Listar platos cercanos (RN-05)",
            description = "Retorna los platos disponibles ordenados por cercanía. La distancia se aproxima a múltiplos de 100m y nunca expone la dirección exacta."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de ofertas cercanas",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlatoCercanoResponseDTO.class)))
            )
    })
    @GetMapping("/cercanos")
    ResponseEntity<List<PlatoCercanoResponseDTO>> listarCercanos(
            @Parameter(description = "Latitud actual del comprador", example = "4.6789")
            @RequestParam(required = false) Double latitud,

            @Parameter(description = "Longitud actual del comprador", example = "-74.0567")
            @RequestParam(required = false) Double longitud
    );

    @Operation(summary = "Eliminar un plato")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> eliminar(@PathVariable UUID id);
}