package com.ollacercana.persistence.mappers;

import com.ollacercana.core.models.Reporte;
import com.ollacercana.persistence.entities.ReporteEntity;
import org.springframework.stereotype.Component;

@Component
public class ReporteEntityMapper {

    public ReporteEntity toEntity(Reporte r) {
        if (r == null) return null;
        return ReporteEntity.builder()
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

    public Reporte toDomain(ReporteEntity e) {
        if (e == null) return null;
        return Reporte.builder()
                .id(e.getId())
                .objetivo(e.getObjetivo())
                .objetivoId(e.getObjetivoId())
                .cuentaObjetivoId(e.getCuentaObjetivoId())
                .motivo(e.getMotivo())
                .descripcion(e.getDescripcion())
                .evidencias(e.getEvidencias())
                .reportanteId(e.getReportanteId())
                .reservaId(e.getReservaId())
                .estado(e.getEstado())
                .fechaCreacion(e.getFechaCreacion())
                .build();
    }
}