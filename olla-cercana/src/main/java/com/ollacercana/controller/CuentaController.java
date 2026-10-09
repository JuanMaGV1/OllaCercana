package com.ollacercana.controller;

import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.docs.CuentaApi;
import com.ollacercana.controller.dtos.request.RegistroRequestDTO;
import com.ollacercana.controller.dtos.response.RegistroResponseDTO;
import com.ollacercana.controller.mappers.CuentaMapper;
import com.ollacercana.core.models.Cuenta;
import com.ollacercana.core.services.ICuentaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cuentas")
@RequiredArgsConstructor
public class CuentaController implements CuentaApi {

    private final ICuentaService cuentaService;
    private final CuentaMapper cuentaMapper;
    private final UsuarioActual usuarioActual;

    @Override
    @PostMapping
    public ResponseEntity<RegistroResponseDTO> registrar(@Valid @RequestBody RegistroRequestDTO request) {

        Cuenta cuentaDominio = cuentaMapper.toDomain(request);


        Cuenta cuentaGuardada = cuentaService.registrar(cuentaDominio);


        RegistroResponseDTO response = cuentaMapper.toResponseDTO(cuentaGuardada);


        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/avisos")
    @PreAuthorize("hasAnyRole('COMPRADOR','COCINERA')")
    public ResponseEntity<Map<String, Boolean>> cambiarAvisos(
            @PathVariable Long id,
            @RequestParam boolean activos) {
        cuentaService.cambiarAvisos(id, activos, usuarioActual.getCuentaId());
        return ResponseEntity.ok(Map.of("avisosActivos", activos));
    }
}