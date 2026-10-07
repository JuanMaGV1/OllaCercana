package com.ollacercana.core.models;

import com.ollacercana.core.models.enums.EstadoReporte;
import com.ollacercana.core.models.enums.MotivoReporte;
import com.ollacercana.core.models.enums.ObjetivoReporte;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reporte {

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