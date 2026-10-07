package com.ollacercana.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.RestriccionAlimentaria;
import com.ollacercana.core.models.enums.TipoComida;
import com.ollacercana.core.patterns.filter.FiltroCompuestoPlato;
import com.ollacercana.core.patterns.filter.FiltroDistanciaMaxima;
import com.ollacercana.core.patterns.filter.FiltroRestricciones;
import com.ollacercana.core.patterns.filter.FiltroTipoComida;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FiltrosPlatoTest {

    @Test
    @DisplayName("FiltroTipoComida: tipoEsperado null o coincidente retorna true, diferente retorna false")
    void filtroTipoComida_evaluacion() {
        Plato plato = Plato.builder().tipoComida(TipoComida.ALMUERZO).build();

        assertTrue(new FiltroTipoComida(null).cumple(plato));
        assertTrue(new FiltroTipoComida(TipoComida.ALMUERZO).cumple(plato));
        assertFalse(new FiltroTipoComida(TipoComida.CENA).cumple(plato));
    }

    @Test
    @DisplayName("FiltroDistanciaMaxima: Coordenadas null en usuario o plato retorna true")
    void filtroDistanciaMaxima_coordenadasNulas_retornaTrue() {
        Plato platoSinCoords = Plato.builder().build();
        assertTrue(new FiltroDistanciaMaxima(null, -74.0, 2000.0).cumple(platoSinCoords));
        assertTrue(new FiltroDistanciaMaxima(4.6, null, 2000.0).cumple(platoSinCoords));
        assertTrue(new FiltroDistanciaMaxima(4.6, -74.0, 2000.0).cumple(platoSinCoords));
    }

    @Test
    @DisplayName("FiltroDistanciaMaxima: Distancia dentro del rango retorna true, fuera retorna false")
    void filtroDistanciaMaxima_distancias() {
        // Coordenadas cercanas (~500m) y lejanas (>10km) en Bogotá
        Plato platoCercano = Plato.builder().latitud(4.6800).longitud(-74.0550).build();

        FiltroDistanciaMaxima filtro = new FiltroDistanciaMaxima(4.6789, -74.0567, 1000.0);
        assertTrue(filtro.cumple(platoCercano));

        FiltroDistanciaMaxima filtroEstricto = new FiltroDistanciaMaxima(4.6789, -74.0567, 50.0);
        assertFalse(filtroEstricto.cumple(platoCercano));
    }

    @Test
    @DisplayName("FiltroRestricciones: evalúa restricciones presentes y nulas")
    void filtroRestricciones_evaluacion() {
        Plato platoSinRestricciones = Plato.builder().restricciones(null).build();
        assertTrue(new FiltroRestricciones(null).cumple(platoSinRestricciones));
        assertTrue(new FiltroRestricciones(List.of()).cumple(platoSinRestricciones));
        assertFalse(new FiltroRestricciones(List.of(RestriccionAlimentaria.SIN_GLUTEN)).cumple(platoSinRestricciones));

        Plato platoConGlutenYVeg = Plato.builder()
                .restricciones(List.of(RestriccionAlimentaria.SIN_GLUTEN, RestriccionAlimentaria.VEGETARIANO))
                .build();

        assertTrue(new FiltroRestricciones(List.of(RestriccionAlimentaria.SIN_GLUTEN)).cumple(platoConGlutenYVeg));
        assertFalse(new FiltroRestricciones(List.of(RestriccionAlimentaria.SIN_LACTOSA)).cumple(platoConGlutenYVeg));
    }

    @Test
    @DisplayName("FiltroCompuestoPlato: allMatch AND y agregar null seguro")
    void filtroCompuestoPlato_evaluacion() {
        FiltroCompuestoPlato compuesto = new FiltroCompuestoPlato();
        compuesto.agregar(null); // No debe fallar

        Plato plato = Plato.builder()
                .tipoComida(TipoComida.ALMUERZO)
                .restricciones(List.of(RestriccionAlimentaria.SIN_GLUTEN))
                .build();

        compuesto.agregar(new FiltroTipoComida(TipoComida.ALMUERZO));
        assertTrue(compuesto.cumple(plato));

        compuesto.agregar(new FiltroRestricciones(List.of(RestriccionAlimentaria.SIN_LACTOSA)));
        assertFalse(compuesto.cumple(plato));
    }
}