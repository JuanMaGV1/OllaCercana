package com.ollacercana.controller.handlers.exception;

public class CuentaNoEncontradaException extends ResourceNotFoundException {
    public CuentaNoEncontradaException(Long id) {
        super("Cuenta no encontrada con id: " + id);
    }
}
