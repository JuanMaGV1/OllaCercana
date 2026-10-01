package com.ollacercana.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public final class GeoUtils {

    private static final double RADIO_TIERRA_METROS = 6371000.0;

    private GeoUtils() {}

    /**
     * Calcula la distancia en metros entre dos coordenadas geográficas usando la fórmula de Haversine.
     */
    public static double calcularDistanciaEnMetros(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIO_TIERRA_METROS * c;
    }

    /**
     * RN-05: Redondea la distancia a múltiplos de 100 metros para no revelar la ubicación exacta.
     * Ejemplo: 850m -> 900m, 840m -> 800m.
     */
    public static int redondearDistanciaMultiplo100(double distanciaMetros) {
        return (int) (Math.round(distanciaMetros / 100.0) * 100);
    }

    /**
     * Formatea el tiempo restante hasta la expiración en formato legible (ej: "3h 20m").
     */
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
}