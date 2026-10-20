package com.ollacercana.controller.handlers.exception;

import com.ollacercana.core.models.enums.EstadoReserva;

public class ReservaNoCompletadaException extends ReglaDeNegocioException {
    public ReservaNoCompletadaException(EstadoReserva estado) {
        super("Solo se pueden calificar reservas COMPLETADAS. Estado actual: " + estado);
    }
}