package com.ollacercana.dto.request;

import com.ollacercana.domain.TipoAjustePorciones;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AjusteDisponibilidadRequest(

        @NotNull(message = "El tipo de ajuste es obligatorio")
        TipoAjustePorciones tipo,

        @Positive(message = "La cantidad debe ser mayor a cero")
        Integer cantidad,

        String motivo,

        @NotNull(message = "La versión es obligatoria para validar concurrencia")
        Integer version

) {}