package com.ollacercana.core.models.enums;

/**
 * Estados de un reporte sobre una reserva.
 * Se define lo mínimo que HU-23 necesita (bloquear el cierre mientras haya un reporte ABIERTO).
 */
public enum EstadoReporte {
    ABIERTO,
    RESUELTO
}
