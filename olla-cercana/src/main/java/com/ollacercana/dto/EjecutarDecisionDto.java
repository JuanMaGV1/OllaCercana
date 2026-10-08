package com.ollacercana.dto;

import com.ollacercana.domain.TipoDecision;
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
public class EjecutarDecisionDto {
    @NotNull(message = "La decisión es obligatoria")
    private TipoDecision decision;

    @NotBlank(message = "Debe proveer una justificación")
    private String justificacion;
}
