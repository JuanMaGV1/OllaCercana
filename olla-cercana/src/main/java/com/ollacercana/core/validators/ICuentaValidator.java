package com.ollacercana.core.validators;

public interface ICuentaValidator {

    void validarCorreoUnico(String correo);

    void validarCelularUnico(String celular);

    void validarPasswordSegura(String password);
}