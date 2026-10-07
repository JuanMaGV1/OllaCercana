package com.ollacercana.controller.handlers.exception;

// Se lanza cuando una cocinera alcanza el numero maximo de platos activos simultaneos (RN-28).
// 409: los datos del nuevo plato son válidos; lo que impide crearlo es el estado actual (ya tiene 3 activos).
public class LimitePlatosActivosExcedidoException extends ConflictoException {

    public LimitePlatosActivosExcedidoException(int max) {
        super("Maximo " + max + " platos activos al mismo tiempo (RN-28)");
    }
}