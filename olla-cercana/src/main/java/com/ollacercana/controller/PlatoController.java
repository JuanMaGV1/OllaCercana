package com.ollacercana.controller;

import com.ollacercana.domain.Plato;
import com.ollacercana.controller.docs.PlatoApi;
import com.ollacercana.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.dto.response.PlatoResponseDTO;
import com.ollacercana.mapper.PlatoMapper;
import com.ollacercana.service.PlatoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * OC-93: PlatoService trabaja únicamente con el dominio Plato.
 */
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
        Plato plato = platoService.obtenerPorId(id);
        return ResponseEntity.ok(platoMapper.toResponse(plato));
    }

    @Override
    public ResponseEntity<PlatoResponseDTO> actualizar(UUID id, AjusteDisponibilidadRequest request) {
        Plato actualizado = platoService.ajustarDisponibilidad(id, request);
        return ResponseEntity.ok(platoMapper.toResponse(actualizado));
    }

    @Override
    public ResponseEntity<Void> eliminar(UUID id) {
        platoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
