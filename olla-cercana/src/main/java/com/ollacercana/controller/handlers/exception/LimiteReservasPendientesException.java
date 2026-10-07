package com.ollacercana.controller.handlers.exception;

// RN-15: El comprador tiene 2 o más reservas pendientes (409)
public class LimiteReservasPendientesException extends ConflictoException {
    public LimiteReservasPendientesException() {
        super("Ya tienes el máximo de reservas pendientes permitidas (máximo 2) (RN-15)");
    }
}