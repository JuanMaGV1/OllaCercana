package com.ollacercana.controller;

import com.ollacercana.controller.docs.ReservaApi;
import com.ollacercana.domain.Reserva;
import com.ollacercana.dto.request.CierreTransaccionRequestDTO;
import com.ollacercana.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.dto.response.ReservaResponseDTO;
import com.ollacercana.mapper.ReservaMapper;
import com.ollacercana.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReservaController implements ReservaApi {

    private final ReservaService reservaService;
    private final ReservaMapper reservaMapper;

    @Override
    public ResponseEntity<ReservaResponseDTO> decidir(UUID id, UUID cocineraId, DecisionReservaRequestDTO request) {
        Reserva actualizada = reservaService.decidir(id, cocineraId, request);
        return ResponseEntity.ok(reservaMapper.toResponse(actualizada));
    }

    @Override
    public ResponseEntity<ReservaResponseDTO> completar(UUID id, CierreTransaccionRequestDTO request) {
        Reserva completada = reservaService.completar(id, request.comentario());
        return ResponseEntity.ok(reservaMapper.toResponse(completada));
    }

    @Override
    public ResponseEntity<List<ReservaResponseDTO>> listarPendientes(UUID cocineraId) {
        return ResponseEntity.ok(reservaMapper.toResponseList(reservaService.listarPendientesDeCocinera(cocineraId)));
    }

    @Override
    public ResponseEntity<ReservaResponseDTO> obtenerPorId(UUID id) {
        return ResponseEntity.ok(reservaMapper.toResponse(reservaService.obtenerPorId(id)));
    }
}
