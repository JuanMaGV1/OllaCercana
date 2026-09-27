package com.ollacercana.exception;

// Se lanza cuando un plato supera el maximo de restricciones alimentarias permitidas (RN-30).
public class LimiteRestriccionesExcedidoException extends BusinessRuleException {

    public LimiteRestriccionesExcedidoException(int max) {
        super("Maximo " + max + " restricciones alimentarias (RN-30)");
    }
}