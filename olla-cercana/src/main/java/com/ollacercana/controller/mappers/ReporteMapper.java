package com.ollacercana.controller.mappers;

import com.ollacercana.controller.dtos.response.ReporteDTO;
import com.ollacercana.core.models.Reporte;
import org.springframework.stereotype.Component;

@Component
public class ReporteMapper {

    public ReporteDTO toDto(Reporte r) {
        if (r == null) return null;
        return ReporteDTO.builder()
                .id(r.getId())
                .objetivo(r.getObjetivo())
                .objetivoId(r.getObjetivoId())
                .cuentaObjetivoId(r.getCuentaObjetivoId())
                .motivo(r.getMotivo())
                .descripcion(r.getDescripcion())
                .evidencias(r.getEvidencias())
                .reportanteId(r.getReportanteId())
                .reservaId(r.getReservaId())
                .estado(r.getEstado())
                .fechaCreacion(r.getFechaCreacion())
                .build();
    }
}
