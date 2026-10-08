package com.ollacercana.exception;

                                                                                                                 
public class ReduccionPorDebajoDeComprometidasException extends BusinessRuleException {
    public ReduccionPorDebajoDeComprometidasException(int comprometidas) {
        super("Tienes " + comprometidas + " porciones reservadas");
    }
}