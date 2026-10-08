package com.ollacercana.controller.handlers.exception;

                                                                                             
public class PorcionesFueraDeRangoException extends BusinessRuleException {

    public PorcionesFueraDeRangoException(int min, int max) {
        super("Las porciones deben estar entre " + min + " y " + max + " (RN-27)");
    }
}