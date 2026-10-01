package com.ollacercana.controller;

import com.ollacercana.controller.docs.PlatoApi;
import com.ollacercana.mapper.PlatoMapper;
import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.model.dto.request.PlatoRequestDTO;
import com.ollacercana.model.dto.response.PlatoCercanoResponseDTO;
import com.ollacercana.model.dto.response.PlatoResponseDTO;
import com.ollacercana.service.PlatoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PlatoController implements PlatoApi {

    private final PlatoService platoService;
    private final PlatoMapper platoMapper;

    @Override
    public ResponseEntity<PlatoResponseDTO> crear(UUID cocineraId, PlatoRequestDTO request) {
        Plato plato = platoMapper.toDomain(request);
        plato.setCocineraId(cocineraId);
        Plato guardado = platoService.crear(plato);
        return ResponseEntity.status(HttpStatus.CREATED).body(platoMapper.toResponse(guardado));
    }

    @Override
    public ResponseEntity<PlatoResponseDTO> obtenerPorId(UUID id) {
        return ResponseEntity.ok(platoMapper.toResponse(platoService.obtenerPorId(id)));
    }

    @Override
    public ResponseEntity<PlatoResponseDTO> actualizar(UUID id, AjusteDisponibilidadRequest request) {
        return ResponseEntity.ok(platoMapper.toResponse(platoService.ajustarDisponibilidad(id, request)));
    }

    @Override
    public ResponseEntity<List<PlatoCercanoResponseDTO>> listarCercanos(Double latitud, Double longitud) {
        return ResponseEntity.ok(platoService.buscarCercanos(latitud, longitud));
    }

    @Override
    public ResponseEntity<Void> eliminar(UUID id) {
        platoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}