package com.ollacercana.service;

import com.ollacercana.model.domain.Cuenta;

public interface ICuentaService {

    /**
     * Registra una nueva cuenta en el sistema previa validación y hasheo de contraseña.
     *
     * @param cuenta Objeto de dominio con la información de la cuenta
     * @return Objeto de dominio guardado y persistido
     */
    Cuenta registrar(Cuenta cuenta);
    Cuenta autenticar(String identificador, String password);
}