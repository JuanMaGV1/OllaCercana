package com.ollacercana.filter;

import com.ollacercana.domain.Plato;
import com.ollacercana.domain.TipoComida;

public record FiltroTipoComida(TipoComida tipoEsperado) implements FiltroPlato {
    @Override
    public boolean cumple(Plato plato) {
        return tipoEsperado == null || tipoEsperado.equals(plato.getTipoComida());
    }
}