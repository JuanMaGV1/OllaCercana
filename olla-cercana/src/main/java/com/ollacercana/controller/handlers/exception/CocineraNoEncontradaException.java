package com.ollacercana.controller.handlers.exception;

                                                                         
public class CocineraNoEncontradaException extends RuntimeException {

    public CocineraNoEncontradaException(java.util.UUID cocineraId) {
        super("Cocinera no encontrada: " + cocineraId);
    }
}
