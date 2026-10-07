package com.ollacercana.core.patterns.filter;

import com.ollacercana.core.models.Plato;
import com.ollacercana.core.util.GeoUtils;

public record FiltroDistanciaMaxima(Double latUsuario, Double lonUsuario, double distanciaMaxMetros) implements FiltroPlato {
    @Override
    public boolean cumple(Plato plato) {
        if (latUsuario == null || lonUsuario == null || plato.getLatitud() == null || plato.getLongitud() == null) {
            return true; // No filtra por distancia si no hay coordenadas
        }
        double distancia = GeoUtils.calcularDistanciaEnMetros(latUsuario, lonUsuario, plato.getLatitud(), plato.getLongitud());
        return distancia <= distanciaMaxMetros;
    }
}