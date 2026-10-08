package com.ollacercana.core.patterns.filter;

import java.util.ArrayList;
import java.util.List;

import com.ollacercana.core.models.Plato;

public class FiltroCompuestoPlato implements FiltroPlato {

    private final List<FiltroPlato> filtros = new ArrayList<>();

    public FiltroCompuestoPlato agregar(FiltroPlato filtro) {
        if (filtro != null) {
            filtros.add(filtro);
        }
        return this;
    }

    @Override
    public boolean cumple(Plato plato) {
                                                                             
        return filtros.stream().allMatch(f -> f.cumple(plato));
    }
}