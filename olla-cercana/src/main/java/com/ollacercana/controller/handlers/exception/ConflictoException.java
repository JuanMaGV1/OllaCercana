package com.ollacercana.controller.handlers.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)       
public class ConflictoException extends RuntimeException {
    public ConflictoException(String message) {
        super(message);
    }
}