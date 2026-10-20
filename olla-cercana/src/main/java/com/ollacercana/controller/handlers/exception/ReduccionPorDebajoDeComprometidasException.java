package com.ollacercana.controller.handlers.exception;

                                                                                                                 
public class ReduccionPorDebajoDeComprometidasException extends BusinessRuleException {
    public ReduccionPorDebajoDeComprometidasException(int comprometidas) {
        super("Tienes " + comprometidas + " porciones reservadas");
    }
}