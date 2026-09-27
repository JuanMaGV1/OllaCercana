package com.ollacercana.exception;

import java.util.UUID;

public class PlatoNoEncontradoException extends ResourceNotFoundException {
    public PlatoNoEncontradoException(UUID id) {
        super("Plato", id);
    }
}