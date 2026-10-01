package com.ollacercana.mapper;

import com.ollacercana.model.domain.Plato;
import com.ollacercana.persistence.entity.PlatoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlatoEntityMapper {

    PlatoEntity toEntity(Plato domain);

    Plato toDomain(PlatoEntity entity);
}