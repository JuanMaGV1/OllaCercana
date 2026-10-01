package com.ollacercana.controller.docs;

import com.ollacercana.model.dto.response.ReservaResponseDTO;
import com.ollacercana.model.dto.request.CierreTransaccionRequestDTO;
import com.ollacercana.model.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.model.dto.request.ReservaRequestDTO;
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

@Tag(name = "Reservas", description = "API para creación y administración de reservas de porciones")
@RequestMapping("/api/v1/reservas")
public interface ReservaApi {

    @Operation(summary = "Crear una nueva reserva", description = "Crea una reserva PENDIENTE y descuenta porciones atómicamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva creada"),
            @ApiResponse(responseCode = "404", description = "Plato no encontrado"),
            @ApiResponse(responseCode = "409", description = "Sin porciones / límite de pendientes"),
            @ApiResponse(responseCode = "422", description = "Auto-reserva (RN-14)")
    })
    @PostMapping
    ResponseEntity<ReservaResponseDTO> crear(
            @Parameter(description = "ID del comprador") @RequestHeader("X-Comprador-Id") Long compradorId,
            @Valid @RequestBody ReservaRequestDTO request);

    @Operation(summary = "Confirmar o rechazar una reserva (HU-12)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva actualizada"),
            @ApiResponse(responseCode = "403", description = "No es tu reserva"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya gestionada / vencida (RN-04)"),
            @ApiResponse(responseCode = "422", description = "Regla de negocio")
    })
    @PatchMapping("/{id}/decision")
    ResponseEntity<ReservaResponseDTO> decidir(
            @PathVariable UUID id,
            @RequestHeader("X-Cocinera-Id") UUID cocineraId,
            @Valid @RequestBody DecisionReservaRequestDTO request);

    @Operation(summary = "Listar solicitudes pendientes de la cocinera (HU-12)")
    @ApiResponse(responseCode = "200", description = "Solicitudes pendientes",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReservaResponseDTO.class))))
    @GetMapping("/pendientes")
    ResponseEntity<List<ReservaResponseDTO>> listarPendientes(
            @RequestHeader("X-Cocinera-Id") UUID cocineraId);

    @Operation(summary = "Confirmar entrega y pago, y cerrar la transacción (HU-23)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva completada"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
            @ApiResponse(responseCode = "422", description = "No está CONFIRMADA o tiene reporte abierto")
    })
    @PostMapping("/{id}/completar")
    ResponseEntity<ReservaResponseDTO> completar(
            @PathVariable UUID id,
            @Valid @RequestBody CierreTransaccionRequestDTO request);

    @Operation(summary = "Consultar una reserva por id")
    @GetMapping("/{id}")
    ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable UUID id);
}