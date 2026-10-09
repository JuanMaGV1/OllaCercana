package com.ollacercana.controller.handlers.exception;

import com.ollacercana.core.models.enums.MedioPago;

/**
 * OC-255: Se lanza cuando el comprador selecciona un método de pago que la cocinera no acepta.
 */
public class MedioPagoNoAceptadoException extends OllaCercanaException {

    public MedioPagoNoAceptadoException(MedioPago medioPago) {
        super(String.format("El medio de pago '%s' no es aceptado por la cocinera para este plato", medioPago));
    }

    public MedioPagoNoAceptadoException(String mensaje) {
        super(mensaje);
    }
}