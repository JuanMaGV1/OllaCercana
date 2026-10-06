package com.ollacercana.exception;

import java.math.BigDecimal;

// Se lanza cuando el precio de un plato esta fuera del rango permitido (RN-27).
public class PrecioFueraDeRangoException extends BusinessRuleException {

    public PrecioFueraDeRangoException(BigDecimal min, BigDecimal max) {
        super("El precio debe estar entre $" + min + " y $" + max + " (RN-27)");
    }
}