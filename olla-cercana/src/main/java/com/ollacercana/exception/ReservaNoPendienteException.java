package com.ollacercana.exception;

import com.ollacercana.domain.EstadoReserva;

// HU-12: solo se puede confirmar o rechazar una reserva PENDIENTE.
// 409: la petición es válida, pero el estado actual de la reserva no lo permite.
public class ReservaNoPendienteException extends ConflictoException {
    public ReservaNoPendienteException(EstadoReserva estadoActual) {
        super("Esta solicitud ya fue gestionada (estado actual: " + estadoActual
                + "). Solo se pueden confirmar o rechazar reservas PENDIENTES");
    }
}
