package com.ollacercana.controller.dtos.request;

import com.ollacercana.core.models.enums.TipoDecision;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EjecutarDecisionDTO {
    @NotNull(message = "La decisión es obligatoria")
    private TipoDecision decision;

    @NotBlank(message = "Debe proveer una justificación")
    private String justificacion;
}