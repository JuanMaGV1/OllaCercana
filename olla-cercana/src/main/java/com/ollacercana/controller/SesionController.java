package com.ollacercana.controller;

import com.ollacercana.config.security.JwtService;
import com.ollacercana.controller.docs.SesionApi;
import com.ollacercana.controller.dtos.request.LoginRequestDTO;
import com.ollacercana.controller.dtos.response.LoginResponseDTO;
import com.ollacercana.core.models.Cuenta;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.services.ICuentaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sesiones")
@RequiredArgsConstructor
public class SesionController implements SesionApi {

    private final ICuentaService cuentaService;
    private final JwtService jwtService;

    @Override
    @PostMapping
    public ResponseEntity<LoginResponseDTO> iniciarSesion(@Valid @RequestBody LoginRequestDTO request) {
        Cuenta cuenta = cuentaService.autenticar(request.getIdentificador(), request.getContrasena());

        String token = jwtService.generarToken(cuenta);

        List<String> listaRoles = (cuenta.getRoles() != null)
                ? cuenta.getRoles().stream().map(Rol::name).toList()
                : Collections.emptyList();

        Rol rolPrincipal = (cuenta.getRoles() != null && !cuenta.getRoles().isEmpty())
                ? cuenta.getRoles().iterator().next()
                : null;

        LoginResponseDTO response = LoginResponseDTO.builder()
                .token(token)
                .id(cuenta.getId())
                .nombre(cuenta.getIdentidad() != null ? cuenta.getIdentidad().getNombre() : null)
                .correo(cuenta.getIdentidad() != null ? cuenta.getIdentidad().getCorreo() : null)
                .rol(rolPrincipal)
                .roles(listaRoles)
                .build();

        return ResponseEntity.ok(response);
    }
}