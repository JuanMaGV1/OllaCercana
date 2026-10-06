package com.ollacercana.filter;

import java.util.List;

import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.domain.RestriccionAlimentaria;

public record FiltroRestricciones(List<RestriccionAlimentaria> requeridas) implements FiltroPlato {
    @Override
    public boolean cumple(Plato plato) {
        if (requeridas == null || requeridas.isEmpty()) return true;
        if (plato.getRestricciones() == null) return false;
        return plato.getRestricciones().containsAll(requeridas);
    }
}