package com.ollacercana.exception;

// RN-14: La cocinera no puede reservar su propio plato (422)
public class AutoReservaException extends BusinessRuleException {
    public AutoReservaException() {
        super("No puedes reservar tu propio plato (RN-14)");
    }
}