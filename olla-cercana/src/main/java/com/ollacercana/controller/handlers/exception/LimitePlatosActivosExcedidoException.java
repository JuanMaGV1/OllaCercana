package com.ollacercana.controller.handlers.exception;

                                                                                               
                                                                                                              
public class LimitePlatosActivosExcedidoException extends ConflictoException {

    public LimitePlatosActivosExcedidoException(int max) {
        super("Maximo " + max + " platos activos al mismo tiempo (RN-28)");
    }
}