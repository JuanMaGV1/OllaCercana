package com.ollacercana.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Método de pago admitido en la plataforma",
        enumAsRef = true)
public enum MedioPago {
    NEQUI,
    DAVIPLATA,
    EFECTIVO,
    TRANSFERENCIA_BANCARIA
}