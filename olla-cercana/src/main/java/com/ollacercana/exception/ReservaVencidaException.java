package com.ollacercana.exception;

                                                                                                
public class ReservaVencidaException extends ConflictoException {
    public ReservaVencidaException() {
        super("El tiempo para responder esta solicitud ya venció (RN-04); "
                + "la reserva expirará y las porciones volverán a la publicación");
    }
}
