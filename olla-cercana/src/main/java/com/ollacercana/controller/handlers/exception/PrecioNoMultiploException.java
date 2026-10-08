package com.ollacercana.controller.handlers.exception;

                                                                       
public class PrecioNoMultiploException extends BusinessRuleException {

    public PrecioNoMultiploException() {
        super("El precio debe ser multiplo de 100 (RN-27)");
    }
}