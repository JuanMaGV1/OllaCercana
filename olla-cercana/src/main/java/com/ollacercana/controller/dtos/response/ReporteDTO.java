package com.ollacercana.controller.dtos.response;

import com.ollacercana.core.models.enums.EstadoReporte;
import com.ollacercana.core.models.enums.MotivoReporte;
import com.ollacercana.core.models.enums.ObjetivoReporte;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDTO {
    private UUID id;
    private ObjetivoReporte objetivo;
    private UUID objetivoId;
    private Long cuentaObjetivoId;
    private MotivoReporte motivo;
    private String descripcion;
    private List<String> evidencias;
    private Long reportanteId;
    private UUID reservaId;
    private EstadoReporte estado;
    private LocalDateTime fechaCreacion;
}