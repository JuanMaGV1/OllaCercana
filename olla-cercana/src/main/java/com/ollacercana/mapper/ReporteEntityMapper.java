package com.ollacercana.mapper;

import com.ollacercana.model.domain.Reporte;
import com.ollacercana.persistence.entity.ReporteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ReporteEntityMapper {
    ReporteEntity toEntity(Reporte reporte);
    Reporte toDomain(ReporteEntity entity);
}