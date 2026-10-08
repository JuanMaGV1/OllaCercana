package com.ollacercana.controller.handlers.exception;

                                                              
public class PrecioObligatorioException extends BusinessRuleException {

    public PrecioObligatorioException() {
        super("El precio es obligatorio");
    }
}