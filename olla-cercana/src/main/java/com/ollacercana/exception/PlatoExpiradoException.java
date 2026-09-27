package com.ollacercana.exception;

// Se lanza al intentar modificar un plato cuyo estado ya es EXPIRADO (RN-29).
public class PlatoExpiradoException extends BusinessRuleException {

    public PlatoExpiradoException() {
        super("Un plato expirado no se puede modificar (RN-29)");
    }
}