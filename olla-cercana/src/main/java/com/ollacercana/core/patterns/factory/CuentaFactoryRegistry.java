package com.ollacercana.core.patterns.factory;

import com.ollacercana.controller.dtos.request.RegistroRequestDTO;
import com.ollacercana.controller.handlers.exception.ReglaDeNegocioException;
import com.ollacercana.core.models.Cuenta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OC-001: registra las fábricas disponibles y delega según el rol solicitado.
 * Extensible sin tocar CuentaServiceImpl (OCP).
 */
@Component
@RequiredArgsConstructor
public class CuentaFactoryRegistry {

    private final List<CuentaFactory> fabricas;

    public Cuenta crear(RegistroRequestDTO request) {
        return fabricas.stream()
                .filter(f -> f.soporta(request))
                .findFirst()
                .orElseThrow(() -> new ReglaDeNegocioException(
                        "Rol no soportado: " + request.getRol()))
                .crear(request);
    }
}