package com.ollacercana.core.models.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Medio de pago aceptado por la plataforma", enumAsRef = true)
public enum MedioPago {
    NEQUI,
    DAVIPLATA,
    EFECTIVO,
    TRANSFERENCIA_BANCARIA
}