package com.ollacercana.mapper;

import com.ollacercana.model.domain.Reporte;
import com.ollacercana.persistence.entity.ReporteEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReporteEntityMapper {

    ReporteEntity toEntity(Reporte domain);

    Reporte toDomain(ReporteEntity entity);
}