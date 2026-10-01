package com.ollacercana.validator.chain;

import com.ollacercana.domain.Plato;

public interface ValidadorPlatoHandler {
    void setSiguiente(ValidadorPlatoHandler siguiente);
    void validar(Plato plato);
}