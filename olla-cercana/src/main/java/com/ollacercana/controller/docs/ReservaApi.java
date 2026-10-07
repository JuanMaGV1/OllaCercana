package com.ollacercana.controller.docs;

import com.ollacercana.config.OpenApiConfig;
import com.ollacercana.controller.dtos.request.CierreTransaccionRequestDTO;
import com.ollacercana.controller.dtos.request.DecisionReservaRequestDTO;
import com.ollacercana.controller.dtos.request.ReservaRequestDTO;
import com.ollacercana.controller.dtos.response.ErrorResponseDTO;
import com.ollacercana.controller.dtos.response.ReservaResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Reservas", description = "API para creación, decisión y administración de reservas (HU-04, HU-12, HU-23)")
@RequestMapping("/api/v1/reservas")
public interface ReservaApi {

    @Operation(
            summary = "Crear una nueva reserva",
            description = "Crea una reserva en estado PENDIENTE deduciendo la identidad del comprador desde el token JWT.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente",
                    content = @Content(schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado (Requiere rol COMPRADOR)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Plato no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Sin porciones suficientes o límite de pendientes",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "La cocinera no puede reservar su propio plato",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping
    ResponseEntity<ReservaResponseDTO> crear(@Valid @RequestBody ReservaRequestDTO request);

    @Operation(
            summary = "Confirmar o rechazar una solicitud de reserva (HU-12)",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva actualizada",
                    content = @Content(schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de la decisión inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "La reserva no pertenece a la cocinera",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "La reserva ya fue gestionada o venció (RN-04)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Decisión inválida según reglas de negocio",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/{id}/decision")
    ResponseEntity<ReservaResponseDTO> decidir(
            @Parameter(description = "Id de la reserva") @PathVariable UUID id,
            @Valid @RequestBody DecisionReservaRequestDTO request
    );

    @Operation(
            summary = "Listar las solicitudes pendientes de la cocinera (HU-12)",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @GetMapping("/pendientes")
    ResponseEntity<List<ReservaResponseDTO>> listarPendientes();

    @Operation(
            summary = "Confirmar entrega y pago, y cerrar la transacción (HU-23)",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @PostMapping("/{id}/completar")
    ResponseEntity<ReservaResponseDTO> completar(
            @Parameter(description = "Id de la reserva") @PathVariable UUID id,
            @Valid @RequestBody CierreTransaccionRequestDTO request
    );

    @Operation(
            summary = "Consultar una reserva por id",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @GetMapping("/{id}")
    ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable UUID id);
}