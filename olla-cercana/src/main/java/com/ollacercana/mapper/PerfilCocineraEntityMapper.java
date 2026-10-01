package com.ollacercana.mapper;

import com.ollacercana.model.domain.PerfilCocinera;
import com.ollacercana.persistence.entity.PerfilCocineraEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PerfilCocineraEntityMapper {

    PerfilCocineraEntity toEntity(PerfilCocinera domain);

    PerfilCocinera toDomain(PerfilCocineraEntity entity);
}