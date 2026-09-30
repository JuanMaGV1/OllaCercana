package com.ollacercana.security;

import org.springframework.stereotype.Service;

import com.ollacercana.model.domain.Cuenta;

@Service
public class JwtService {

    // Pendiente Sprint 3: Implementación real de generación de token
    public String generarToken(Cuenta cuenta) {
        return "mock-jwt-token-pendiente-sprint-3";
    }

    // Pendiente Sprint 3: Implementación real de validación de token
    public boolean validarToken(String token) {
        return true;
    }
}