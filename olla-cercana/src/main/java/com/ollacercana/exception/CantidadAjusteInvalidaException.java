package com.ollacercana.exception;

                                                                                    
public class CantidadAjusteInvalidaException extends BusinessRuleException {
    public CantidadAjusteInvalidaException() {
        super("La cantidad debe ser mayor a cero");
    }
}