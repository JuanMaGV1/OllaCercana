package com.ollacercana.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoUtilsTest {

    @Test
    @DisplayName("RN-05: Redondeo de distancia a múltiplos de 100m")
    void redondearDistanciaMultiplo100_VerificaCasosEjemplo() {

        assertEquals(900, GeoUtils.redondearDistanciaMultiplo100(850.0));
        assertEquals(800, GeoUtils.redondearDistanciaMultiplo100(840.0));
        assertEquals(100, GeoUtils.redondearDistanciaMultiplo100(120.0));
        assertEquals(1000, GeoUtils.redondearDistanciaMultiplo100(999.0));
        assertEquals(0, GeoUtils.redondearDistanciaMultiplo100(40.0));
    }

    @Test
    @DisplayName("formatearTiempoRestante: evalúa null, expirado, horas+minutos y solo minutos")
    void formatearTiempoRestante_ramas() {
        assertEquals("0m", GeoUtils.formatearTiempoRestante(null));

        // Ya expirado en el pasado
        assertEquals("Expirado", GeoUtils.formatearTiempoRestante(java.time.LocalDateTime.now().minusMinutes(5)));

        // Horas y minutos en el futuro
        String resultadoHoras = GeoUtils.formatearTiempoRestante(java.time.LocalDateTime.now().plusHours(2).plusMinutes(15));
        assertTrue(resultadoHoras.contains("2h"));

        // Menos de una hora en el futuro (solo minutos)
        String resultadoMinutos = GeoUtils.formatearTiempoRestante(java.time.LocalDateTime.now().plusMinutes(30));
        assertFalse(resultadoMinutos.contains("h"));
        assertTrue(resultadoMinutos.contains("m"));
    }
}