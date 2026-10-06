package com.ollacercana.filter;

import com.ollacercana.model.domain.Plato;
import com.ollacercana.util.GeoUtils;

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