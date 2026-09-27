package com.ollacercana.validator;

public interface ICuentaValidator {

    void validarCorreoUnico(String correo);

    void validarCelularUnico(String celular);

    void validarPasswordSegura(String password);
}