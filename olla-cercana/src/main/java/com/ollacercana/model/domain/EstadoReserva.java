package com.ollacercana.model.domain;

/**
 * Estados posibles de una reserva (ver diagrama de clases).
 */
public enum EstadoReserva {
    PENDIENTE,
    CONFIRMADA,
    RECHAZADA,
    EXPIRADA,
    CANCELADA,
    COMPLETADA
}