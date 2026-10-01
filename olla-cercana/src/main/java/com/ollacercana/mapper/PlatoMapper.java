package com.ollacercana.mapper;

import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.dto.request.PlatoRequestDTO;
import com.ollacercana.model.dto.response.PlatoResponseDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * OC-89: Sin logica manual.
 */
@Mapper(componentModel = "spring")
public interface PlatoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cocineraId", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "porcionesComprometidas", ignore = true)
    @Mapping(target = "fechaPublicacion", ignore = true)
    @Mapping(target = "fechaExpiracion", ignore = true)
    Plato toDomain(PlatoRequestDTO dto);

    PlatoResponseDTO toResponse(Plato plato);
}
