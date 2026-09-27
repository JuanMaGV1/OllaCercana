package com.ollacercana.mapper;

import com.ollacercana.domain.Plato;
import org.mapstruct.Mapper;

/**
 * OC-89: mapper dominio <-> entidad de persistencia.
 */

@Mapper(componentModel = "spring")
public interface PlatoEntityMapper {

    Plato toEntity(Plato domain);

    Plato toDomain(Plato entity);
}
