package com.ollacercana.exception;

// Se lanza cuando otra operación modificó la reserva o el plato al mismo tiempo (bloqueo optimista).
public class ReservaModificadaException extends ConflictoException {
    public ReservaModificadaException() {
        super("La reserva o el plato cambiaron mientras se procesaba la solicitud, refresca e intenta de nuevo");
    }
}
