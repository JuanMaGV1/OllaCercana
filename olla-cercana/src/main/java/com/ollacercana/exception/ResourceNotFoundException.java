package com.ollacercana.exception;

import java.util.UUID;

public class ResourceNotFoundException extends OllaCercanaException {
    public ResourceNotFoundException(String recurso, UUID id) {
        super(recurso + " no encontrado con id: " + id);
    }
}