package com.ollacercana.exception;

// Se lanza cuando el numero de porciones de un plato esta fuera del rango permitido (RN-27).
public class PorcionesFueraDeRangoException extends BusinessRuleException {

    public PorcionesFueraDeRangoException(int min, int max) {
        super("Las porciones deben estar entre " + min + " y " + max + " (RN-27)");
    }
}