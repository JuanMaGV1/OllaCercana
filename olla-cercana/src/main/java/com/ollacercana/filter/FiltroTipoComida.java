package com.ollacercana.filter;

import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.domain.TipoComida;

public record FiltroTipoComida(TipoComida tipoEsperado) implements FiltroPlato {
    @Override
    public boolean cumple(Plato plato) {
        return tipoEsperado == null || tipoEsperado.equals(plato.getTipoComida());
    }
}