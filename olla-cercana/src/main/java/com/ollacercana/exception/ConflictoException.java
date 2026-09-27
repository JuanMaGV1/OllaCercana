package com.ollacercana.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409
public class ConflictoException extends RuntimeException {
    public ConflictoException(String message) {
        super(message);
    }
}