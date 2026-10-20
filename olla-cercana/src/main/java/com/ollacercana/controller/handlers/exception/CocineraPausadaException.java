package com.ollacercana.controller.handlers.exception;

                                                                                
                                         
public class CocineraPausadaException extends ConflictoException {

    public CocineraPausadaException() {
        super("Tu perfil está pausado, no puedes publicar platos");
    }
}