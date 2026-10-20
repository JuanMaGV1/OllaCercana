package com.ollacercana.controller.handlers.exception;

import java.math.BigDecimal;

                                                                                
public class PrecioFueraDeRangoException extends BusinessRuleException {

    public PrecioFueraDeRangoException(BigDecimal min, BigDecimal max) {
        super("El precio debe estar entre $" + min + " y $" + max + " (RN-27)");
    }
}