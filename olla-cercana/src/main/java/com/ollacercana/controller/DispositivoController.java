package com.ollacercana.controller;

import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.docs.DispositivoApi;
import com.ollacercana.controller.dtos.request.TokenDispositivoRequestDTO;
import com.ollacercana.core.services.ICuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DispositivoController implements DispositivoApi {

    private final ICuentaService cuentaService;
    private final UsuarioActual usuarioActual;

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> registrarToken(@Valid TokenDispositivoRequestDTO request) {
        cuentaService.registrarTokenDispositivo(usuarioActual.getCuentaId(), request.token());
        return ResponseEntity.ok(Map.of("mensaje", "Token del dispositivo registrado exitosamente"));
    }
}