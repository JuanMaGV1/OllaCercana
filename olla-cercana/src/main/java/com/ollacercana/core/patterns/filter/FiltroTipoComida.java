package com.ollacercana.core.patterns.filter;

import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.TipoComida;

public record FiltroTipoComida(TipoComida tipoEsperado) implements FiltroPlato {
    @Override
    public boolean cumple(Plato plato) {
        return tipoEsperado == null || tipoEsperado.equals(plato.getTipoComida());
    }
}