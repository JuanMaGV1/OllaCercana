package com.ollacercana.controller.handlers.exception;

import java.util.UUID;

// Se lanza cuando se consulta o se decide sobre una reserva que no existe (404).
public class ReservaNoEncontradaException extends com.ollacercana.controller.handlers.exception.ResourceNotFoundException {
    public ReservaNoEncontradaException(UUID id) {
        super("Reserva no encontrada con id: " + id);
    }
}
