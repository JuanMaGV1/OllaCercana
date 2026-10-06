package com.ollacercana.validator.chain;

import com.ollacercana.model.domain.Plato;

public abstract class AbstractValidadorPlatoHandler implements ValidadorPlatoHandler {

    protected ValidadorPlatoHandler siguiente;

    @Override
    public void setSiguiente(ValidadorPlatoHandler siguiente) {
        this.siguiente = siguiente;
    }

    protected void pasarAlSiguiente(Plato plato) {
        if (siguiente != null) {
            siguiente.validar(plato);
        }
    }
}