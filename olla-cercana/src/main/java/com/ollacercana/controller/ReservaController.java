package com.ollacercana.controller;

import com.ollacercana.controller.docs.ReservaApi;
import com.ollacercana.mapper.ReservaMapper;
import com.ollacercana.model.dto.request.CierreTransaccionRequestDTO;
import com.ollacercana.model.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.model.dto.request.ReservaRequestDTO;
import com.ollacercana.model.dto.response.ReservaResponseDTO;
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
    public ResponseEntity<ReservaResponseDTO> crear(UUID compradorId, ReservaRequestDTO request) {
        ReservaResponseDTO response = reservaService.crear(compradorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ReservaResponseDTO> decidir(UUID id, UUID cocineraId, DecisionReservaRequestDTO request) {
        return ResponseEntity.ok(reservaMapper.toResponse(reservaService.decidir(id, cocineraId, request)));
    }

    @Override
    public ResponseEntity<List<ReservaResponseDTO>> listarPendientes(UUID cocineraId) {
        return ResponseEntity.ok(reservaMapper.toResponseList(reservaService.listarPendientesDeCocinera(cocineraId)));
    }

    @Override
    public ResponseEntity<ReservaResponseDTO> completar(UUID id, CierreTransaccionRequestDTO request) {
        return ResponseEntity.ok(reservaMapper.toResponse(reservaService.completar(id, request.comentario())));
    }

    @Override
    public ResponseEntity<ReservaResponseDTO> obtenerPorId(UUID id) {
        return ResponseEntity.ok(reservaMapper.toResponse(reservaService.obtenerPorId(id)));
    }
}