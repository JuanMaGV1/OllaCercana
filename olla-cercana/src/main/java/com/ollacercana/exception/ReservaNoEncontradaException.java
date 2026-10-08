package com.ollacercana.exception;

import java.util.UUID;

                                                                                 
public class ReservaNoEncontradaException extends com.ollacercana.exception.ResourceNotFoundException {
    public ReservaNoEncontradaException(UUID id) {
        super("Reserva no encontrada con id: " + id);
    }
}
