package com.ollacercana.core.validators.chain;

import com.ollacercana.core.models.Plato;

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