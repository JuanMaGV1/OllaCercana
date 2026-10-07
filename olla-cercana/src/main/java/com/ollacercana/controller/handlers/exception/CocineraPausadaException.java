package com.ollacercana.controller.handlers.exception;

// Se lanza al intentar publicar un Plato cuya cocinera tiene su perfil pausado.
// 409: conflicto de estado, no de datos.
public class CocineraPausadaException extends ConflictoException {

    public CocineraPausadaException() {
        super("Tu perfil está pausado, no puedes publicar platos");
    }
}