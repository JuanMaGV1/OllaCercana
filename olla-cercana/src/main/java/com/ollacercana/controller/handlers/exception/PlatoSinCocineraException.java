package com.ollacercana.controller.handlers.exception;

                                                                  
public class PlatoSinCocineraException extends BusinessRuleException {

    public PlatoSinCocineraException() {
        super("Un plato no puede publicarse sin una cocinera asociada");
    }
}
