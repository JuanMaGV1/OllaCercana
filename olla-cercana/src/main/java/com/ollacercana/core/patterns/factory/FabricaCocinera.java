package com.ollacercana.core.patterns.factory;

import com.ollacercana.core.models.*;
import com.ollacercana.core.models.enums.*;
import com.ollacercana.controller.dtos.request.RegistroRequestDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * OC-001: creación de cuentas COMPRADOR.
 */
@Component
public class FabricaCocinera implements CuentaFactory {

    @Override
    public boolean soporta(RegistroRequestDTO request) {
        return request.getRol() == Rol.COCINERA;
    }

    @Override
    public Cuenta crear(RegistroRequestDTO request) {
        return Cuenta.builder()
                .identidad(new Identidad(request.getNombre(), request.getCorreo(),
                        request.getCelular(), null))
                .credenciales(new Credenciales(request.getContrasena(), null, false))
                .roles(Set.of(Rol.COCINERA))
                .estado(EstadoCuenta.ACTIVO)
                .fechaRegistro(LocalDateTime.now())
                .avisosActivos(true)
                .build();
    }
}