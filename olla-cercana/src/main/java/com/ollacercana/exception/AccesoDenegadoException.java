package com.ollacercana.exception;

// 403: el usuario existe y la petición es válida, pero no tiene permiso sobre el recurso.
public class AccesoDenegadoException extends OllaCercanaException {
    public AccesoDenegadoException(String message) {
        super(message);
    }
}
