package com.ollacercana.core.patterns.factory;

import com.ollacercana.controller.dtos.request.RegistroRequestDTO;
import com.ollacercana.controller.handlers.exception.ReglaDeNegocioException;
import com.ollacercana.core.models.Cuenta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Patrón Factory Method — registro de fábricas por rol.
 *
 * Extensible sin modificar el servicio (OCP). Cada fábrica concreta
 * declara {@code soporta(request)} y encapsula las reglas de creación.
 *
 * @see OC-001 Factory Method de cuentas (RN-01)
 * @see FabricaComprador
 * @see FabricaCocinera
 * @see FabricaAdministrador
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