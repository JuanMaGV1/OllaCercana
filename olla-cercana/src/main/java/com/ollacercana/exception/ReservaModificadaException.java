package com.ollacercana.exception;

                                                                                                     
public class ReservaModificadaException extends ConflictoException {
    public ReservaModificadaException() {
        super("La reserva o el plato cambiaron mientras se procesaba la solicitud, refresca e intenta de nuevo");
    }
}
