package com.ollacercana.exception;

import com.ollacercana.model.domain.EstadoReserva;

// HU-23 Escenario 4: solo se puede cerrar una reserva CONFIRMADA (422).
public class ReservaNoConfirmadaException extends ReglaDeNegocioException {
    public ReservaNoConfirmadaException(EstadoReserva estadoActual) {
        super("Solo se puede cerrar una reserva CONFIRMADA (estado actual: " + estadoActual + ")");
    }
}
