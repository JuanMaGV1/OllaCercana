package com.ollacercana.controller;

import com.ollacercana.controller.docs.ReservaApi;
import com.ollacercana.domain.Reserva;
import com.ollacercana.dto.request.ReservaRequestDTO;
import com.ollacercana.dto.response.ReservaResponseDTO;
import com.ollacercana.mapper.ReservaMapper;
import com.ollacercana.service.IReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaController implements ReservaApi {

    private final IReservaService reservaService;
    private final ReservaMapper reservaMapper;

    @Override
    @PostMapping
    public ResponseEntity<ReservaResponseDTO> crear(
            @RequestHeader("X-Comprador-Id") Long compradorId,
            @Valid @RequestBody ReservaRequestDTO request) {

        Reserva reserva = reservaMapper.toDomain(request);
        ReservaResponseDTO response = reservaService.crear(compradorId, reserva);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}