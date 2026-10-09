package com.ollacercana.utils;

import com.ollacercana.core.util.GeoUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

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
        assertEquals("Expirado", GeoUtils.formatearTiempoRestante(java.time.LocalDateTime.now().minusMinutes(5)));
        String resultadoHoras = GeoUtils.formatearTiempoRestante(java.time.LocalDateTime.now().plusHours(2).plusMinutes(15));
        assertTrue(resultadoHoras.contains("2h"));
        String resultadoMinutos = GeoUtils.formatearTiempoRestante(java.time.LocalDateTime.now().plusMinutes(30));
        assertFalse(resultadoMinutos.contains("h"));
        assertTrue(resultadoMinutos.contains("m"));
    }

    @Test
    @DisplayName("OC-235: Mismo ID de cocinera y misma sal producen exactamente el mismo resultado entre llamadas")
    void ofuscarCoordenadas_mismoIdYMismaSal_mismoResultado() {
        UUID cocineraId = UUID.randomUUID();
        String salt = "mi-sal-secreta-prod";
        double latReal = 4.6789;
        double lonReal = -74.0567;

        GeoUtils.CoordenadasOfuscadas res1 = GeoUtils.ofuscarCoordenadas(cocineraId, salt, latReal, lonReal);
        GeoUtils.CoordenadasOfuscadas res2 = GeoUtils.ofuscarCoordenadas(cocineraId, salt, latReal, lonReal);

        assertEquals(res1.latitud(), res2.latitud(), "Mismo ID y sal deben producir la misma latitud");
        assertEquals(res1.longitud(), res2.longitud(), "Mismo ID y sal deben producir la misma longitud");
    }

    @Test
    @DisplayName("OC-235: Mismo ID con distinta sal produce desplazamientos distintos (no reversible)")
    void ofuscarCoordenadas_mismoIdYDistintaSal_distintoResultado() {
        UUID cocineraId = UUID.randomUUID();
        double latReal = 4.6789;
        double lonReal = -74.0567;

        GeoUtils.CoordenadasOfuscadas res1 = GeoUtils.ofuscarCoordenadas(cocineraId, "sal-alpha", latReal, lonReal);
        GeoUtils.CoordenadasOfuscadas res2 = GeoUtils.ofuscarCoordenadas(cocineraId, "sal-beta", latReal, lonReal);

        assertFalse(res1.latitud() == res2.latitud() && res1.longitud() == res2.longitud(),
                "Sales distintas deben producir desplazamientos diferentes");
    }

    @Test
    @DisplayName("OC-235: IDs distintos producen desplazamientos distintos")
    void ofuscarCoordenadas_idsDistintosProducenDesplazamientosDistintos() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        String salt = "sal-comun";
        double latReal = 4.6789;
        double lonReal = -74.0567;

        GeoUtils.CoordenadasOfuscadas res1 = GeoUtils.ofuscarCoordenadas(id1, salt, latReal, lonReal);
        GeoUtils.CoordenadasOfuscadas res2 = GeoUtils.ofuscarCoordenadas(id2, salt, latReal, lonReal);

        assertFalse(res1.latitud() == res2.latitud() && res1.longitud() == res2.longitud(),
                "IDs distintos deben producir desplazamientos diferentes");
    }

    @Test
    @DisplayName("OC-235: Coordenadas ofuscadas nunca son iguales a las reales y se encuentran entre 100m y 300m")
    void ofuscarCoordenadas_nuncaIgualesYDentroDelMargen() {
        double latReal = 4.6789;
        double lonReal = -74.0567;

        for (int i = 0; i < 50; i++) {
            UUID cocineraId = UUID.randomUUID();
            GeoUtils.CoordenadasOfuscadas ofuscada = GeoUtils.ofuscarCoordenadas(cocineraId, latReal, lonReal);

            assertFalse(Double.compare(latReal, ofuscada.latitud()) == 0, "La latitud ofuscada no debe ser idéntica a la real");
            assertFalse(Double.compare(lonReal, ofuscada.longitud()) == 0, "La longitud ofuscada no debe ser idéntica a la real");

            double distancia = GeoUtils.calcularDistanciaEnMetros(latReal, lonReal, ofuscada.latitud(), ofuscada.longitud());
            assertTrue(distancia >= GeoUtils.MARGEN_MIN_OFUSCACION_METROS * 0.99,
                    "Distancia " + distancia + " debe ser >= " + GeoUtils.MARGEN_MIN_OFUSCACION_METROS);
            assertTrue(distancia <= GeoUtils.MARGEN_MAX_OFUSCACION_METROS * 1.01,
                    "Distancia " + distancia + " debe ser <= " + GeoUtils.MARGEN_MAX_OFUSCACION_METROS);
        }
    }

    @Test
    @DisplayName("OC-235: Ofuscación determinística con distancia y ángulo fijos")
    void ofuscarCoordenadas_deterministica() {
        double latReal = 4.6789;
        double lonReal = -74.0567;
        double distanciaDeseada = 200.0;
        double angulo = Math.PI / 4;

        GeoUtils.CoordenadasOfuscadas ofuscada = GeoUtils.ofuscarCoordenadas(latReal, lonReal, distanciaDeseada, angulo);
        double distCalculada = GeoUtils.calcularDistanciaEnMetros(latReal, lonReal, ofuscada.latitud(), ofuscada.longitud());

        assertEquals(distanciaDeseada, distCalculada, 2.0, "La distancia debe coincidir aproximadamente con los 200 metros");
    }
}