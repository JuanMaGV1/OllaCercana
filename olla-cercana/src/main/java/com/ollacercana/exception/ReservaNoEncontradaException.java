package com.ollacercana.exception;

import java.util.UUID;

// Se lanza cuando se consulta o se decide sobre una reserva que no existe (404).
public class ReservaNoEncontradaException extends com.ollacercana.exception.ResourceNotFoundException {
    public ReservaNoEncontradaException(UUID id) {
        super("Reserva no encontrada con id: " + id);
    }
}
