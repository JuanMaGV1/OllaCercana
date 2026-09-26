package com.ollacercana.controller;

import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.dto.response.PlatoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Platos", description = "Gestión de ofertas de comida casera")
@RequestMapping("/api/v1/platos")
public interface PlatoApi {

    @Operation(summary = "Publicar un nuevo plato (HU-04)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plato publicado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (validación de formulario)"),
            @ApiResponse(responseCode = "409", description = "Regla de negocio violada: precio/porciones fuera de rango, " +
                    "límite de 3 platos activos por cocinera, o cocinera no verificada/pausada")
    })
    @PostMapping
    ResponseEntity<PlatoResponseDTO> crear(
            @RequestHeader("X-Cocinera-Id") UUID cocineraId,
            @Valid @RequestBody PlatoRequestDTO request
    );
}