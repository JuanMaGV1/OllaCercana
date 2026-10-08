package com.ollacercana.exception;

import com.ollacercana.domain.EstadoReserva;

                                                                        
public class ReservaNoConfirmadaException extends ReglaDeNegocioException {
    public ReservaNoConfirmadaException(EstadoReserva estadoActual) {
        super("Solo se puede cerrar una reserva CONFIRMADA (estado actual: " + estadoActual + ")");
    }
}
