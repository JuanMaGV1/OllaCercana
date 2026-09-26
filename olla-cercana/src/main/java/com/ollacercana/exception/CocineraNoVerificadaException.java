package com.ollacercana.exception;

// Se lanza al intentar publicar un Plato cuya cocinera no ha verificado su teléfono (RN de habilitación).
public class CocineraNoVerificadaException extends BusinessRuleException {

    public CocineraNoVerificadaException() {
        super("Debes verificar tu teléfono antes de publicar un plato");
    }
}