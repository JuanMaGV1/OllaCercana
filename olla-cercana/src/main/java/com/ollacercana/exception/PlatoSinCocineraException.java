package com.ollacercana.exception;

// Se lanza al intentar publicar un Plato sin cocineraId asignado.
public class PlatoSinCocineraException extends BusinessRuleException {

    public PlatoSinCocineraException() {
        super("Un plato no puede publicarse sin una cocinera asociada");
    }
}
