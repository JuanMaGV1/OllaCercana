package com.ollacercana.exception;

// Se lanza cuando una cocinera alcanza el numero maximo de platos activos simultaneos (RN-28).
public class LimitePlatosActivosExcedidoException extends BusinessRuleException {

    public LimitePlatosActivosExcedidoException(int max) {
        super("Maximo " + max + " platos activos al mismo tiempo (RN-28)");
    }
}