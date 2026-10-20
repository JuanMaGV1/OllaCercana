package com.ollacercana.controller.handlers.exception;

                                                              
public class LimiteReservasPendientesException extends ConflictoException {
    public LimiteReservasPendientesException() {
        super("Ya tienes el máximo de reservas pendientes permitidas (máximo 2) (RN-15)");
    }
}