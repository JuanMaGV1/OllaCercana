package com.ollacercana.controller.handlers.exception;

// RN-03: No hay porciones disponibles suficientes (409)
public class PorcionesInsuficientesException extends ConflictoException {
    public PorcionesInsuficientesException(int disponibles) {
        super("No hay suficientes porciones disponibles. Disponibles: " + disponibles);
    }
}