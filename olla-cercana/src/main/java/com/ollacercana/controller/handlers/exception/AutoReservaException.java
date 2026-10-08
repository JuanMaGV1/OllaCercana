package com.ollacercana.controller.handlers.exception;

                                                             
public class AutoReservaException extends BusinessRuleException {
    public AutoReservaException() {
        super("No puedes reservar tu propio plato (RN-14)");
    }
}