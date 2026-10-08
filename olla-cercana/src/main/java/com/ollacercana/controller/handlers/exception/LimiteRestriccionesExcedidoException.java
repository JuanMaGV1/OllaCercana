package com.ollacercana.controller.handlers.exception;

                                                                                              
public class LimiteRestriccionesExcedidoException extends BusinessRuleException {

    public LimiteRestriccionesExcedidoException(int max) {
        super("Maximo " + max + " restricciones alimentarias (RN-30)");
    }
}