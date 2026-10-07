package com.ollacercana.exception;

                                                                                
                                         
public class CocineraPausadaException extends ConflictoException {

    public CocineraPausadaException() {
        super("Tu perfil está pausado, no puedes publicar platos");
    }
}