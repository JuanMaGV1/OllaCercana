package com.ollacercana.persistence.mappers;

import com.ollacercana.core.models.Plato;
import com.ollacercana.persistence.entities.PlatoEntity;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PlatoEntityMapper {

    PlatoEntity toEntity(Plato plato);

    Plato toDomain(PlatoEntity entity);

    List<Plato> toDomainList(List<PlatoEntity> entities);
}