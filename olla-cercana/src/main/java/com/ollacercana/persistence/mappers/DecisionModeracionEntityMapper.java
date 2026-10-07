package com.ollacercana.persistence.mappers;

import com.ollacercana.core.models.DecisionModeracion;
import com.ollacercana.core.models.Reporte;
import com.ollacercana.persistence.entities.DecisionModeracionEntity;
import org.springframework.stereotype.Component;

@Component
public class DecisionModeracionEntityMapper {

    private final ReporteEntityMapper reporteMapper;

    public DecisionModeracionEntityMapper(ReporteEntityMapper reporteMapper) {
        this.reporteMapper = reporteMapper;
    }

    public DecisionModeracionEntity toEntity(DecisionModeracion d) {
        if (d == null) return null;
        return DecisionModeracionEntity.builder()
                .id(d.getId())
                .decision(d.getDecision())
                .justificacion(d.getJustificacion())
                .administradorId(d.getAdministradorId())
                .fecha(d.getFecha())
                .reporte(d.getReporte() == null ? null : reporteMapper.toEntity(d.getReporte()))
                .build();
    }

    public DecisionModeracion toDomain(DecisionModeracionEntity e) {
        if (e == null) return null;
        return DecisionModeracion.builder()
                .id(e.getId())
                .decision(e.getDecision())
                .justificacion(e.getJustificacion())
                .administradorId(e.getAdministradorId())
                .fecha(e.getFecha())
                .reporte(e.getReporte() == null ? null : reporteMapper.toDomain(e.getReporte()))
                .build();
    }
}