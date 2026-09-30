package com.ollacercana.mapper;

import org.mapstruct.Mapper;

import com.ollacercana.model.domain.Plato;

/**
 * OC-89: mapper dominio <-> entidad de persistencia.
 */

@Mapper(componentModel = "spring")
public interface PlatoEntityMapper {

    Plato toEntity(Plato domain);

    Plato toDomain(Plato entity);
}
