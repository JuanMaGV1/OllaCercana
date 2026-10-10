package com.ollacercana.controller.handlers.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ReglaDeNegocioException extends BusinessRuleException {
    public ReglaDeNegocioException(String message) {
        super(message);
    }
}