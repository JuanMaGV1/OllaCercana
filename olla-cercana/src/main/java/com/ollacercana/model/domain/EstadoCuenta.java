package com.ollacercana.model.domain;

/**
 * Estados de una cuenta (RN-01, RN-13).
 * - PENDIENTE_VERIFICACION: cuenta creada pero celular sin verificar por OTP.
 * - ACTIVA: cuenta operativa.
 * - PAUSADA: cocinera pausada por baja reputación (RN-09).
 * - BLOQUEADA_TEMPORAL: bloqueo por intentos fallidos (RN-12).
 * - SUSPENDIDA: suspensión por moderación (OC-018).
 */
public enum EstadoCuenta {
    PENDIENTE_VERIFICACION,
    ACTIVA,
    PAUSADA,
    BLOQUEADA_TEMPORAL,
    SUSPENDIDA
}