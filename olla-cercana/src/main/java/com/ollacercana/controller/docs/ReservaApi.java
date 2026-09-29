package com.ollacercana.controller.docs;

import com.ollacercana.dto.request.ReservaRequestDTO;
import com.ollacercana.dto.response.ErrorResponseDTO;
import com.ollacercana.dto.response.ReservaResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Reservas", description = "API para creación y administración de reservas de porciones")
public interface ReservaApi {

    @Operation(
            summary = "Crear una nueva reserva",
            description = "Crea una reserva en estado PENDIENTE, descuenta porciones atómicamente y calcula límite de confirmación a +10 minutos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente",
                    content = @Content(schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Plato no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: sin porciones suficientes, límite de pendientes o colisión concurrente",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regla de negocio: la cocinera no puede reservar su propio plato",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<ReservaResponseDTO> crear(
            @Parameter(description = "ID de la cuenta del comprador (Long devuelto en el login)", example = "1", required = true)
            @RequestHeader("X-Comprador-Id") Long compradorId,
            @Valid @RequestBody ReservaRequestDTO request
    );
}