package com.ollacercana.exception;

// Se lanza cuando se consulta un cocineraId que no existe en el sistema.
public class CocineraNoEncontradaException extends RuntimeException {

    public CocineraNoEncontradaException(java.util.UUID cocineraId) {
        super("Cocinera no encontrada: " + cocineraId);
    }
}
