package com.ollacercana.domain;

/**
 * Estado del chat de coordinación de una reserva (HU-12 / HU-23).
 */
public enum EstadoChat {
    /** Aún no hay chat: la reserva no ha sido confirmada. */
    INACTIVO,
    /** La reserva está confirmada y las partes pueden enviarse mensajes. */
    ACTIVO,
    /** RN-17: la transacción se cerró; el chat solo se puede consultar. */
    SOLO_LECTURA
}
