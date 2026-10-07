package com.ollacercana.core.validators.chain;

import com.ollacercana.core.models.Plato;

public interface ValidadorPlatoHandler {
    void setSiguiente(ValidadorPlatoHandler siguiente);
    void validar(Plato plato);
}