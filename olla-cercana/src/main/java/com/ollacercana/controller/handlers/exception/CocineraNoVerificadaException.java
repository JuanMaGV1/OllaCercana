package com.ollacercana.controller.handlers.exception;

                                                                                                          
                                                                                            
public class CocineraNoVerificadaException extends ConflictoException {

    public CocineraNoVerificadaException() {
        super("Debes verificar tu teléfono antes de publicar un plato");
    }
}