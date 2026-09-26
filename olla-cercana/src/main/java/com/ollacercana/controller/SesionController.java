package com.ollacercana.controller;

import com.ollacercana.controller.docs.SesionApi;
import com.ollacercana.domain.Cuenta;
import com.ollacercana.domain.Rol;
import com.ollacercana.dto.request.LoginRequestDTO;
import com.ollacercana.dto.response.LoginResponseDTO;
import com.ollacercana.security.JwtService;
import com.ollacercana.service.ICuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sesiones")
@RequiredArgsConstructor
public class SesionController implements SesionApi {

    private final ICuentaService cuentaService;
    private final JwtService jwtService;

    @Override
    @PostMapping
    public ResponseEntity<LoginResponseDTO> iniciarSesion(@Valid @RequestBody LoginRequestDTO request) {
        // 1. Autentica las credenciales con el servicio de cuenta
        Cuenta cuenta = cuentaService.autenticar(request.getIdentificador(), request.getContrasena());

        // 2. Genera el token JWT (Mock para Sprint 2)
        String token = jwtService.generarToken(cuenta);

        // Extraer rol principal (primer rol disponible)
        Rol rolPrincipal = (cuenta.getRoles() != null && !cuenta.getRoles().isEmpty())
                ? cuenta.getRoles().iterator().next()
                : null;

        // 3. Construye y retorna la respuesta 200 OK
        LoginResponseDTO response = LoginResponseDTO.builder()
                .token(token)
                .id(cuenta.getId())
                .nombre(cuenta.getIdentidad() != null ? cuenta.getIdentidad().getNombre() : null)
                .correo(cuenta.getIdentidad() != null ? cuenta.getIdentidad().getCorreo() : null)
                .rol(rolPrincipal)
                .build();

        return ResponseEntity.ok(response);
    }
}