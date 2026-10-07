package com.ollacercana.exception;

                                                        
public class PorcionesInsuficientesException extends ConflictoException {
    public PorcionesInsuficientesException(int disponibles) {
        super("No hay suficientes porciones disponibles. Disponibles: " + disponibles);
    }
}