package com.ollacercana.controller.handlers.exception;

// Se lanza cuando se intenta reducir el total por debajo de las porciones ya comprometidas (HU-24, Escenario 2).
public class ReduccionPorDebajoDeComprometidasException extends BusinessRuleException {
    public ReduccionPorDebajoDeComprometidasException(int comprometidas) {
        super("Tienes " + comprometidas + " porciones reservadas");
    }
}