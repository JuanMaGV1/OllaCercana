package com.ollacercana.controller.handlers.exception;

// Se lanza cuando la cantidad de ajuste es nula o no positiva (AUMENTAR/DISMINUIR).
public class CantidadAjusteInvalidaException extends BusinessRuleException {
    public CantidadAjusteInvalidaException() {
        super("La cantidad debe ser mayor a cero");
    }
}