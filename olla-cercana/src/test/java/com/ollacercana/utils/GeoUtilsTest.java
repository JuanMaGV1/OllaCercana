package com.ollacercana.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}