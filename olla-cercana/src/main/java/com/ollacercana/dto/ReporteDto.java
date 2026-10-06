package com.ollacercana.dto;

import com.ollacercana.domain.EstadoReporte;
import com.ollacercana.domain.MotivoReporte;
import com.ollacercana.domain.ObjetivoReporte;
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
public class ReporteDto {
    private UUID id;
    private ObjetivoReporte objetivo;
    private UUID objetivoId;
    private MotivoReporte motivo;
    private String descripcion;
    private List<String> evidencias;
    private UUID reportanteId;
    private UUID reservaId;
    private EstadoReporte estado;
    private LocalDateTime fechaCreacion;
}
