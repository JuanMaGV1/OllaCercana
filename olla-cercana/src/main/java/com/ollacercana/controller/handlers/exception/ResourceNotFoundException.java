package com.ollacercana.controller.handlers.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }

    public ResourceNotFoundException(String entidad, Object id) {
        super(entidad + " no encontrado con id: " + id);
    }
}