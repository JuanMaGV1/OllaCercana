package com.ollacercana.exception;

                                                                                                          
                                                                                            
public class CocineraNoVerificadaException extends ConflictoException {

    public CocineraNoVerificadaException() {
        super("Debes verificar tu teléfono antes de publicar un plato");
    }
}