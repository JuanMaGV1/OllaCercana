package com.ollacercana.controller.docs;

import com.ollacercana.model.dto.request.CierreTransaccionRequestDTO;
import com.ollacercana.model.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.model.dto.request.ReservaRequestDTO;
import com.ollacercana.model.dto.response.ReservaResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Reservas", description = "API para creación y administración de reservas")
@RequestMapping("/api/v1/reservas")
public interface ReservaApi {

    @Operation(summary = "Crear una nueva reserva")
    @PostMapping
    ResponseEntity<ReservaResponseDTO> crear(
            @RequestHeader("X-Comprador-Id") UUID compradorId,   // ← antes Long
            @Valid @RequestBody ReservaRequestDTO request
    );

    @Operation(summary = "Confirmar o rechazar una reserva (HU-12)")
    @PatchMapping("/{id}/decision")
    ResponseEntity<ReservaResponseDTO> decidir(
            @PathVariable UUID id,
            @RequestHeader("X-Cocinera-Id") UUID cocineraId,
            @Valid @RequestBody DecisionReservaRequestDTO request
    );

    @Operation(summary = "Listar solicitudes pendientes de la cocinera")
    @GetMapping("/pendientes")
    ResponseEntity<List<ReservaResponseDTO>> listarPendientes(
            @RequestHeader("X-Cocinera-Id") UUID cocineraId
    );

    @Operation(summary = "Confirmar entrega y pago, cerrar transacción (HU-23)")
    @PostMapping("/{id}/completar")
    ResponseEntity<ReservaResponseDTO> completar(
            @PathVariable UUID id,
            @Valid @RequestBody CierreTransaccionRequestDTO request
    );

    @Operation(summary = "Consultar una reserva por id")
    @GetMapping("/{id}")
    ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable UUID id);
}