package com.ollacercana.controller;

import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.dto.response.PlatoResponseDTO;
import com.ollacercana.service.PlatoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PlatoController implements PlatoApi {

    private final PlatoService platoService;

    @Override
    public ResponseEntity<PlatoResponseDTO> crear(UUID cocineraId, PlatoRequestDTO request) {
        PlatoResponseDTO response = platoService.crear(request, cocineraId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}