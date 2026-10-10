package com.ollacercana.core.patterns.factory;

import com.ollacercana.core.models.Cuenta;
import com.ollacercana.controller.dtos.request.RegistroRequestDTO;

/**
 * OC-001 / RN-01: contrato del Factory Method para creación de cuentas.
 * Cada implementación encapsula las reglas de construcción según el rol.
 */
public interface CuentaFactory {
    boolean soporta(RegistroRequestDTO request);
    Cuenta crear(RegistroRequestDTO request);
}