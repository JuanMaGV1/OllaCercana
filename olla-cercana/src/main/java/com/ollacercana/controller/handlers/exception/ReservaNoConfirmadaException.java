package com.ollacercana.controller.handlers.exception;

import com.ollacercana.core.models.enums.EstadoReserva;

                                                                        
public class ReservaNoConfirmadaException extends ReglaDeNegocioException {
    public ReservaNoConfirmadaException(EstadoReserva estadoActual) {
        super("Solo se puede cerrar una reserva CONFIRMADA (estado actual: " + estadoActual + ")");
    }
}
