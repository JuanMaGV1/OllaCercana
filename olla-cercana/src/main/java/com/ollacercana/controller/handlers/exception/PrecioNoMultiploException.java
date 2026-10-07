package com.ollacercana.controller.handlers.exception;

// Se lanza cuando el precio de un plato no es multiplo de 100 (RN-27).
public class PrecioNoMultiploException extends BusinessRuleException {

    public PrecioNoMultiploException() {
        super("El precio debe ser multiplo de 100 (RN-27)");
    }
}