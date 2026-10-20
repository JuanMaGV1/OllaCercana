package com.ollacercana.controller.dtos.request;

import com.ollacercana.core.models.enums.MotivoReporte;
import com.ollacercana.core.models.enums.ObjetivoReporte;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteCrearDTO {

    @NotNull(message = "El objetivo del reporte es obligatorio")
    private ObjetivoReporte objetivo;

    private UUID objetivoId;
    private Long cuentaObjetivoId;
    private MotivoReporte motivo;
    private String descripcion;
    private List<String> evidencias;
    private UUID reservaId;
}