package com.ollacercana.exception;

                                                                                              
public class LimiteRestriccionesExcedidoException extends BusinessRuleException {

    public LimiteRestriccionesExcedidoException(int max) {
        super("Maximo " + max + " restricciones alimentarias (RN-30)");
    }
}