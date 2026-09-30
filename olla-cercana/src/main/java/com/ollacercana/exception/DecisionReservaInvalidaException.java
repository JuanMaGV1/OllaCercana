package com.ollacercana.exception;

// HU-12: datos de la decisión que no cumplen las reglas (hora estimada, motivo o comentario de rechazo).
public class DecisionReservaInvalidaException extends BusinessRuleException {
    public DecisionReservaInvalidaException(String message) {
        super(message);
    }
}
