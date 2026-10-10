package com.ollacercana.controller.handlers.exception;

public class AccesoDenegadoCalificacionException extends ReglaDeNegocioException {
    public AccesoDenegadoCalificacionException() {
        super("Solo el comprador de la reserva puede calificarla");
    }
}