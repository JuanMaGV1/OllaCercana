package com.ollacercana.controller.handlers.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Unificada con la jerarquía base de excepciones de negocio del dominio (422 Unprocessable Entity).
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ReglaDeNegocioException extends BusinessRuleException {
    public ReglaDeNegocioException(String message) {
        super(message);
    }
}