package com.ollacercana.dto;

import com.ollacercana.domain.MotivoReporte;
import com.ollacercana.domain.ObjetivoReporte;
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
public class ReporteCrearDto {
    @NotNull(message = "El objetivo del reporte es obligatorio")
    private ObjetivoReporte objetivo;

    @NotNull(message = "El id del objetivo es obligatorio")
    private UUID objetivoId;

    @NotNull(message = "Debe seleccionar un motivo de reporte")
    private MotivoReporte motivo;

    private String descripcion;
    private List<String> evidencias;
    private UUID reservaId;
}
