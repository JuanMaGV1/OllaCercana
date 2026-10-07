package com.ollacercana.exception;

                                                              
public class PrecioObligatorioException extends BusinessRuleException {

    public PrecioObligatorioException() {
        super("El precio es obligatorio");
    }
}