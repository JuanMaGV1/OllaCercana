package com.ollacercana.controller;

import com.ollacercana.controller.docs.PerfilCocineraApi;
import com.ollacercana.model.dto.request.PerfilCocineraRequestDTO;
import com.ollacercana.model.dto.request.VerificarOtpRequestDTO;
import com.ollacercana.model.dto.response.PerfilCocineraResponseDTO;
import com.ollacercana.service.IPerfilCocineraService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/perfiles")
@RequiredArgsConstructor
public class PerfilCocineraController implements PerfilCocineraApi {

    private final IPerfilCocineraService perfilService;

    @Override
    @PostMapping
    public ResponseEntity<PerfilCocineraResponseDTO> crearPerfil(@Valid @RequestBody PerfilCocineraRequestDTO request) {
        PerfilCocineraResponseDTO response = perfilService.crearPerfil(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<PerfilCocineraResponseDTO> actualizarPerfil(
            @PathVariable UUID id,
            @Valid @RequestBody PerfilCocineraRequestDTO request) {
        PerfilCocineraResponseDTO response = perfilService.actualizarPerfil(id, request);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/{id}/verificar-telefono")
    public ResponseEntity<Map<String, String>> verificarTelefono(
            @PathVariable UUID id,
            @Valid @RequestBody VerificarOtpRequestDTO request) {
        perfilService.verificarTelefono(id, request.getCodigo());
        return ResponseEntity.ok(Map.of("message", "Teléfono verificado exitosamente"));
    }

    @Override
    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<PerfilCocineraResponseDTO> obtenerPorCuentaId(@PathVariable Long cuentaId) {
        return ResponseEntity.ok(perfilService.obtenerPorCuentaId(cuentaId));
    }

    @Override
    @GetMapping("/destacadas")
    public ResponseEntity<List<PerfilCocineraResponseDTO>> listarDestacadas() {
        return ResponseEntity.ok(perfilService.listarDestacadas());
    }
}