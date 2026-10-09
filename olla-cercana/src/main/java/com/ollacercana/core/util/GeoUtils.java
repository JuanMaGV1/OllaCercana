package com.ollacercana.core.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

public final class GeoUtils {

    private static final double RADIO_TIERRA_METROS = 6371000.0;
    public static final String DEFAULT_SALT = "olla-cercana-default-salt-dev";
    public static final double MARGEN_MIN_OFUSCACION_METROS = 100.0;
    public static final double MARGEN_MAX_OFUSCACION_METROS = 300.0;
    public record CoordenadasOfuscadas(double latitud, double longitud) {}

    private GeoUtils() {}

    public static double calcularDistanciaEnMetros(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIO_TIERRA_METROS * c;
    }

    public static int redondearDistanciaMultiplo100(double distanciaMetros) {
        return (int) (Math.round(distanciaMetros / 100.0) * 100);
    }

    public static String formatearTiempoRestante(LocalDateTime fechaExpiracion) {
        if (fechaExpiracion == null) return "0m";
        LocalDateTime ahora = LocalDateTime.now();
        if (ahora.isAfter(fechaExpiracion)) return "Expirado";

        Duration duracion = Duration.between(ahora, fechaExpiracion);
        long horas = duracion.toHours();
        long minutos = duracion.toMinutesPart();

        if (horas > 0) {
            return horas + "h " + minutos + "m";
        }
        return minutos + "m";
    }

    /**
     * OC-235: Ofusca coordenadas geográficas desplazándolas con un margen acotado (100m a 300m)
     * derivado de forma determinista del ID de la cocinera mezclado con una sal secreta configurable.
     * Mismo ID y misma sal producen exactamente el mismo resultado; diferente sal o diferente ID
     * producen desplazamientos distintos. Las coordenadas ofuscadas nunca coinciden con las reales.
     */
    public static CoordenadasOfuscadas ofuscarCoordenadas(UUID cocineraId, String salt, double latitud, double longitud) {
        long seed;
        if (cocineraId != null) {
            String combined = (salt != null ? salt : "") + ":" + cocineraId;
            long hash = 1125899906842597L;
            for (int i = 0; i < combined.length(); i++) hash = 31L * hash + combined.charAt(i);
            seed = hash;
        } else {
            seed = Double.doubleToLongBits(latitud) ^ Double.doubleToLongBits(longitud);
        }
        Random random = new Random(seed);
        double distancia = MARGEN_MIN_OFUSCACION_METROS
                + random.nextDouble() * (MARGEN_MAX_OFUSCACION_METROS - MARGEN_MIN_OFUSCACION_METROS);
        double angulo = random.nextDouble() * 2 * Math.PI;
        return ofuscarCoordenadas(latitud, longitud, distancia, angulo);
    }

    public static CoordenadasOfuscadas ofuscarCoordenadas(UUID cocineraId, double latitud, double longitud) {
        return ofuscarCoordenadas(cocineraId, DEFAULT_SALT, latitud, longitud);
    }

    public static CoordenadasOfuscadas ofuscarCoordenadas(double latitud, double longitud) {
        return ofuscarCoordenadas(null, DEFAULT_SALT, latitud, longitud);
    }

    /**
     * Variante determinística para pruebas o cálculos con distancia y ángulo fijos.
     */
    public static CoordenadasOfuscadas ofuscarCoordenadas(double latitud, double longitud,
                                                           double distanciaMetros, double anguloRadianes) {
        double dLat = (distanciaMetros * Math.cos(anguloRadianes)) / RADIO_TIERRA_METROS;
        double latRad = Math.toRadians(latitud);
        double divisorLon = RADIO_TIERRA_METROS * Math.cos(latRad);
        if (Math.abs(divisorLon) < 1e-6) divisorLon = 1e-6;
        double dLon = (distanciaMetros * Math.sin(anguloRadianes)) / divisorLon;
        return new CoordenadasOfuscadas(latitud + Math.toDegrees(dLat), longitud + Math.toDegrees(dLon));
    }
}
