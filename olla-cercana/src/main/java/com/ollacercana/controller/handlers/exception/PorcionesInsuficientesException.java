package com.ollacercana.controller.handlers.exception;

                                                        
public class PorcionesInsuficientesException extends ConflictoException {
    public PorcionesInsuficientesException(int disponibles) {
        super("No hay suficientes porciones disponibles. Disponibles: " + disponibles);
    }
}