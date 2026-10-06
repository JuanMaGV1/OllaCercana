package com.ollacercana.filter;

import com.ollacercana.domain.Plato;
import com.ollacercana.domain.RestriccionAlimentaria;
import java.util.List;

public record FiltroRestricciones(List<RestriccionAlimentaria> requeridas) implements FiltroPlato {
    @Override
    public boolean cumple(Plato plato) {
        if (requeridas == null || requeridas.isEmpty()) return true;
        if (plato.getRestricciones() == null) return false;
        return plato.getRestricciones().containsAll(requeridas);
    }
}