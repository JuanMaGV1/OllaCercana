package com.ollacercana.mapper;

import com.ollacercana.domain.Plato;
import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.dto.response.PlatoResponseDTO;
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
    @Mapping(target = "version", ignore = true)
    Plato toDomain(PlatoRequestDTO dto);

    PlatoResponseDTO toResponse(Plato plato);
}
