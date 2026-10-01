package com.ollacercana.controller.docs;

import com.ollacercana.dto.request.CierreTransaccionRequestDTO;
import com.ollacercana.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.dto.request.ReservaRequestDTO;
import com.ollacercana.dto.response.ErrorResponseDTO;
import com.ollacercana.dto.response.ReservaResponseDTO;
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

import java.util.List;
import java.util.UUID;

@Tag(name = "Reservas", description = "API para creación, decisión y administración de reservas (HU-04, HU-12, HU-23)")
@RequestMapping("/api/v1/reservas")
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
    @PostMapping
    ResponseEntity<ReservaResponseDTO> crear(
            @Parameter(description = "ID de la cuenta del comprador", example = "1", required = true)
            @RequestHeader("X-Comprador-Id") Long compradorId,
            @Valid @RequestBody ReservaRequestDTO request
    );

    @Operation(
            summary = "Confirmar o rechazar una solicitud de reserva (HU-12)",
            description = "CONFIRMAR: la reserva pasa a CONFIRMADA. RECHAZAR: la reserva pasa a RECHAZADA y devuelve porciones."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva actualizada",
                    content = @Content(schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de la decisión inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "La reserva no pertenece a la cocinera",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "La reserva ya fue gestionada o venció (RN-04)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "La decisión no cumple las reglas de negocio",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/{id}/decision")
    ResponseEntity<ReservaResponseDTO> decidir(
            @Parameter(description = "Id de la reserva") @PathVariable UUID id,
            @Parameter(description = "Id del perfil de la cocinera") @RequestHeader("X-Cocinera-Id") UUID cocineraId,
            @Valid @RequestBody DecisionReservaRequestDTO request
    );

    @Operation(summary = "Listar las solicitudes pendientes de la cocinera (HU-12)")
    @ApiResponse(responseCode = "200", description = "Solicitudes pendientes",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReservaResponseDTO.class))))
    @GetMapping("/pendientes")
    ResponseEntity<List<ReservaResponseDTO>> listarPendientes(
            @Parameter(description = "Id del perfil de la cocinera") @RequestHeader("X-Cocinera-Id") UUID cocineraId
    );

    @Operation(summary = "Confirmar entrega y pago, y cerrar la transacción (HU-23)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva completada",
                    content = @Content(schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos del cierre inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "La reserva fue modificada al mismo tiempo",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "La reserva no está CONFIRMADA o tiene reporte abierto",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/{id}/completar")
    ResponseEntity<ReservaResponseDTO> completar(
            @Parameter(description = "Id de la reserva") @PathVariable UUID id,
            @Valid @RequestBody CierreTransaccionRequestDTO request
    );

    @Operation(summary = "Consultar una reserva por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada",
                    content = @Content(schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/{id}")
    ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable UUID id);
}