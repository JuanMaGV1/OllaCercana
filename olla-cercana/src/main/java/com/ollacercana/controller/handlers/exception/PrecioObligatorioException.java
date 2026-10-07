package com.ollacercana.controller.handlers.exception;

// Se lanza al intentar publicar un plato sin precio asignado.
public class PrecioObligatorioException extends BusinessRuleException {

    public PrecioObligatorioException() {
        super("El precio es obligatorio");
    }
}