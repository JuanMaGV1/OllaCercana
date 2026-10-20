package com.ollacercana.controller.handlers.exception;

public class CalificacionDuplicadaException extends ReglaDeNegocioException {
    public CalificacionDuplicadaException() {
        super("Esta reserva ya fue calificada");
    }
}