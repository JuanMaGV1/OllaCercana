package com.ollacercana.controller;

import com.ollacercana.controller.docs.SesionApi;
import com.ollacercana.model.domain.Cuenta;
import com.ollacercana.model.domain.Rol;
import com.ollacercana.model.dto.request.LoginRequestDTO;
import com.ollacercana.model.dto.response.LoginResponseDTO;
import com.ollacercana.security.JwtService;
import com.ollacercana.service.ICuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        Rol rolPrincipal = (cuenta.getRoles() != null && !cuenta.getRoles().isEmpty())
                ? cuenta.getRoles().iterator().next()
                : null;

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