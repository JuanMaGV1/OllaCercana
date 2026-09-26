package com.ollacercana.exception;

// Se lanza al intentar publicar un Plato cuya cocinera tiene su perfil pausado.
public class CocineraPausadaException extends BusinessRuleException {

    public CocineraPausadaException() {
        super("Tu perfil está pausado, no puedes publicar platos");
    }
}