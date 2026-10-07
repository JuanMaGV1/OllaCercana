package com.ollacercana.controller.handlers.exception;

// Se lanza al intentar publicar un Plato cuya cocinera no ha verificado su teléfono (RN de habilitación).
// 409: el dato del plato es válido, pero el estado actual de la cocinera bloquea la acción.
public class CocineraNoVerificadaException extends ConflictoException {

    public CocineraNoVerificadaException() {
        super("Debes verificar tu teléfono antes de publicar un plato");
    }
}