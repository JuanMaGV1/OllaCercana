package com.ollacercana.exception;

// RN-04: la cocinera tiene 10 minutos para responder. Pasado ese tiempo la reserva expira sola.
public class ReservaVencidaException extends ConflictoException {
    public ReservaVencidaException() {
        super("El tiempo para responder esta solicitud ya venció (RN-04); "
                + "la reserva expirará y las porciones volverán a la publicación");
    }
}
