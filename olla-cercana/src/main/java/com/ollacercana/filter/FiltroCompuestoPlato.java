package com.ollacercana.filter;

import com.ollacercana.domain.Plato;
import java.util.ArrayList;
import java.util.List;

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
        // Debe cumplir con todos los filtros añadidos (Operación lógica AND)
        return filtros.stream().allMatch(f -> f.cumple(plato));
    }
}