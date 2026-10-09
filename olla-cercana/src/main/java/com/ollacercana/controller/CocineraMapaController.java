package com.ollacercana.controller;

import com.ollacercana.controller.docs.CocineraMapaApi;
import com.ollacercana.controller.dtos.request.MapaCocinerasRequestDTO;
import com.ollacercana.controller.dtos.response.CocineraMapaResponseDTO;
import com.ollacercana.core.services.CocineraMapaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cocineras")
@RequiredArgsConstructor
public class CocineraMapaController implements CocineraMapaApi {

    private final CocineraMapaService cocineraMapaService;

    @Override
    @GetMapping("/mapa")
    public ResponseEntity<List<CocineraMapaResponseDTO>> consultarMapa(
            @Valid @ParameterObject MapaCocinerasRequestDTO request) {
        List<CocineraMapaResponseDTO> resultado = cocineraMapaService.buscarCocinerasEnMapa(request);
        return ResponseEntity.ok(resultado);
    }
}
