package com.ollacercana.controller.mappers;

import com.ollacercana.controller.dtos.request.PlatoRequestDTO;
import com.ollacercana.controller.dtos.response.PlatoResponseDTO;
import com.ollacercana.core.models.Plato;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PlatoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cocineraId", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "porcionesComprometidas", ignore = true)
    @Mapping(target = "fechaPublicacion", ignore = true)
    @Mapping(target = "fechaExpiracion", ignore = true)
    @Mapping(target = "version", ignore = true)
    Plato toDomain(PlatoRequestDTO dto);

    @Mapping(target = "mediosPago", ignore = true)
    @Mapping(target = "notaPago", ignore = true)
    @Mapping(target = "withMediosPago", ignore = true)
    PlatoResponseDTO toResponse(Plato plato);
    List<PlatoResponseDTO> toResponseList(List<Plato> platos);
}