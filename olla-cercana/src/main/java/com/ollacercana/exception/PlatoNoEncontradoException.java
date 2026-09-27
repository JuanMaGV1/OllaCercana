package com.ollacercana.exception;

import java.util.UUID;

// Se lanza cuando no existe un plato con el id solicitado.
public class PlatoNoEncontradoException extends BusinessRuleException {
    public PlatoNoEncontradoException(UUID id) {
        super("No se encontro el plato con id " + id);
    }
}