package com.ollacercana.model.domain;

/**
 * Estado del chat ligado a una reserva (RN-17).
 * - ABIERTO: la reserva está Confirmada y ambos pueden escribir.
 * - SOLO_LECTURA: la reserva se cerró; solo se puede consultar.
 */
public enum EstadoChat {
    ABIERTO,
    SOLO_LECTURA
}