package com.ollacercana.core.models;

import com.ollacercana.core.models.enums.TipoDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionModeracion {

    private UUID id;
    private TipoDecision decision;
    private String justificacion;
    private Long administradorId;
    private LocalDateTime fecha;
    private Reporte reporte;
}