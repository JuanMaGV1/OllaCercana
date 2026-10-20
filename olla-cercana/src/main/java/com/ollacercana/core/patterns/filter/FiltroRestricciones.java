package com.ollacercana.core.patterns.filter;

import java.util.List;

import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.RestriccionAlimentaria;

public record FiltroRestricciones(List<RestriccionAlimentaria> requeridas) implements FiltroPlato {
    
    @Override
    public boolean cumple(Plato plato) {
        if (requeridas == null || requeridas.isEmpty()) return true;
        if (plato.getRestricciones() == null) return false;
        return plato.getRestricciones().containsAll(requeridas);
    }
}