package com.ollacercana.controller;

import com.ollacercana.controller.docs.ReservaApi;
import com.ollacercana.model.dto.response.ReservaResponseDTO;
import com.ollacercana.mapper.ReservaMapper;
import com.ollacercana.model.dto.request.CierreTransaccionRequestDTO;
import com.ollacercana.model.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.model.dto.request.ReservaRequestDTO;
import com.ollacercana.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaController implements ReservaApi {

    private final ReservaService reservaService;
    private final ReservaMapper reservaMapper;

    @Override
    @PostMapping
    public ResponseEntity<ReservaResponseDTO> crear(
            @RequestHeader("X-Comprador-Id") Long compradorId,
            @Valid @RequestBody ReservaRequestDTO request) {
        var reserva = reservaService.crear(compradorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reserva);
    }

    @Override
    @PatchMapping("/{id}/decision")
    public ResponseEntity<ReservaResponseDTO> decidir(
            @PathVariable UUID id,
            @RequestHeader("X-Cocinera-Id") UUID cocineraId,
            @Valid @RequestBody DecisionReservaRequestDTO request) {
        var reserva = reservaService.decidir(id, cocineraId, request);
        return ResponseEntity.ok(reservaMapper.toResponse(reserva));
    }

    @Override
    @GetMapping("/pendientes")
    public ResponseEntity<List<ReservaResponseDTO>> listarPendientes(
            @RequestHeader("X-Cocinera-Id") UUID cocineraId) {
        return ResponseEntity.ok(
                reservaMapper.toResponseList(reservaService.listarPendientesDeCocinera(cocineraId)));
    }

    @Override
    @PostMapping("/{id}/completar")
    public ResponseEntity<ReservaResponseDTO> completar(
            @PathVariable UUID id,
            @Valid @RequestBody CierreTransaccionRequestDTO request) {
        var completada = reservaService.completar(id, request.comentario());
        return ResponseEntity.ok(reservaMapper.toResponse(completada));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(reservaMapper.toResponse(reservaService.obtenerPorId(id)));
    }
}