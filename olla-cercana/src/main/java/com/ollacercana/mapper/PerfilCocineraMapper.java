package com.ollacercana.mapper;

import com.ollacercana.model.domain.PerfilCocinera;
import com.ollacercana.model.dto.request.PerfilCocineraRequestDTO;
import com.ollacercana.model.dto.response.PerfilCocineraResponseDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PerfilCocineraMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "promedioCalificacion", ignore = true)
    @Mapping(target = "resenasPositivas", ignore = true)
    @Mapping(target = "esDestacada", ignore = true)
    @Mapping(target = "verificada", ignore = true)
    @Mapping(target = "pausada", ignore = true)
    @Mapping(target = "cuenta", ignore = true)
    PerfilCocinera toDomain(PerfilCocineraRequestDTO request);

    @Mapping(target = "cuentaId", source = "cuenta.id")
    @Mapping(target = "nombreCocinera", source = "cuenta.identidad.nombre")
    PerfilCocineraResponseDTO toResponseDTO(PerfilCocinera perfil);
}