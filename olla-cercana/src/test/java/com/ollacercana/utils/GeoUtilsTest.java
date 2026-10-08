package com.ollacercana.utils;

import com.ollacercana.core.util.GeoUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

                                   
        assertEquals("Expirado", GeoUtils.formatearTiempoRestante(java.time.LocalDateTime.now().minusMinutes(5)));

                                       
        String resultadoHoras = GeoUtils.formatearTiempoRestante(java.time.LocalDateTime.now().plusHours(2).plusMinutes(15));
        assertTrue(resultadoHoras.contains("2h"));

                                                        
        String resultadoMinutos = GeoUtils.formatearTiempoRestante(java.time.LocalDateTime.now().plusMinutes(30));
        assertFalse(resultadoMinutos.contains("h"));
        assertTrue(resultadoMinutos.contains("m"));
    }

    @Test
    @DisplayName("OC-235/OC-236: Coordenadas ofuscadas nunca coinciden con las reales y se mantienen en el margen")
    void ofuscarCoordenadas_VerificaDiferenciaYMargen() {
        double latReal = 4.6789;
        double lonReal = -74.0567;

        double[] ofuscadas = GeoUtils.ofuscarCoordenadas(latReal, lonReal);

        assertNotEquals(latReal, ofuscadas[0]);
        assertNotEquals(lonReal, ofuscadas[1]);

        double distancia = GeoUtils.calcularDistanciaEnMetros(latReal, lonReal, ofuscadas[0], ofuscadas[1]);
        assertTrue(distancia > 50.0 && distancia <= 150.0,
                "La distancia entre real y ofuscada debe estar cerca del margen de seguridad configurado");
    }
}