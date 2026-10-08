package com.ollacercana.exception;

import com.ollacercana.domain.EstadoReserva;

                                                                   
                                                                                 
public class ReservaNoPendienteException extends ConflictoException {
    public ReservaNoPendienteException(EstadoReserva estadoActual) {
        super("Esta solicitud ya fue gestionada (estado actual: " + estadoActual
                + "). Solo se pueden confirmar o rechazar reservas PENDIENTES");
    }
}
