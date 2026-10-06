package com.ollacercana.controller;

import com.ollacercana.controller.docs.PerfilCocineraApi;
import com.ollacercana.mapper.PerfilCocineraMapper;
import com.ollacercana.model.domain.PerfilCocinera;
import com.ollacercana.model.dto.request.PerfilCocineraRequestDTO;
import com.ollacercana.model.dto.request.VerificarOtpRequestDTO;
import com.ollacercana.model.dto.response.PerfilCocineraResponseDTO;
import com.ollacercana.service.IPerfilCocineraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/perfiles")
@RequiredArgsConstructor
public class PerfilCocineraController implements PerfilCocineraApi {

    private final IPerfilCocineraService perfilService;
    private final PerfilCocineraMapper perfilMapper;   // ← presentación

    @Override
    @PostMapping
    @PreAuthorize("hasRole('COCINERA') or hasRole('ADMIN')")
    public ResponseEntity<PerfilCocineraResponseDTO> crearPerfil(
            @Valid @RequestBody PerfilCocineraRequestDTO request) {
        PerfilCocinera dominio = perfilMapper.toDomain(request);
        PerfilCocinera creado = perfilService.crearPerfil(dominio, request.getCuentaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(perfilMapper.toResponseDTO(creado));
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COCINERA') or hasRole('ADMIN')")
    public ResponseEntity<PerfilCocineraResponseDTO> actualizarPerfil(
            @PathVariable UUID id,
            @Valid @RequestBody PerfilCocineraRequestDTO request) {
        PerfilCocinera dominio = perfilMapper.toDomain(request);
        PerfilCocinera actualizado = perfilService.actualizarPerfil(id, dominio);
        return ResponseEntity.ok(perfilMapper.toResponseDTO(actualizado));
    }

    @Override
    @PostMapping("/{id}/verificar-telefono")
    @PreAuthorize("hasRole('COCINERA') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> verificarTelefono(
            @PathVariable UUID id,
            @Valid @RequestBody VerificarOtpRequestDTO request) {
        perfilService.verificarTelefono(id, request.getCodigo());
        return ResponseEntity.ok(Map.of("mensaje", "Teléfono verificado exitosamente"));
    }

    @Override
    @GetMapping("/cuenta/{cuentaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PerfilCocineraResponseDTO> obtenerPorCuentaId(@PathVariable Long cuentaId) {
        PerfilCocinera perfil = perfilService.obtenerPorCuentaId(cuentaId);
        return ResponseEntity.ok(perfilMapper.toResponseDTO(perfil));
    }

    @Override
    @GetMapping("/destacadas")
    public ResponseEntity<List<PerfilCocineraResponseDTO>> listarDestacadas() {
        List<PerfilCocinera> destacadas = perfilService.listarDestacadas();
        return ResponseEntity.ok(perfilMapper.toResponseList(destacadas));
    }
}