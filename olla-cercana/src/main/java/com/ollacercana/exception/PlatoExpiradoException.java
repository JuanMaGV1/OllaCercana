package com.ollacercana.exception;

                                                                              
public class PlatoExpiradoException extends BusinessRuleException {

    public PlatoExpiradoException() {
        super("Un plato expirado no se puede modificar (RN-29)");
    }
}