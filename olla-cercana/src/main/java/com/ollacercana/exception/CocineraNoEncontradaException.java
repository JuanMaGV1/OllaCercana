package com.ollacercana.exception;

                                                                         
public class CocineraNoEncontradaException extends RuntimeException {

    public CocineraNoEncontradaException(java.util.UUID cocineraId) {
        super("Cocinera no encontrada: " + cocineraId);
    }
}
