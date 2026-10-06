package com.ollacercana.mapper;

import com.ollacercana.model.domain.PerfilCocinera;
import com.ollacercana.model.dto.request.PerfilCocineraRequestDTO;
import com.ollacercana.model.dto.response.PerfilCocineraResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper de PRESENTACIÓN para PerfilCocinera.
 * Traduce entre RequestDTO ↔ dominio y dominio → ResponseDTO.
 * Lo usa el Controller.
 *
 * No confundir con PerfilCocineraEntityMapper (persistencia: dominio ↔ entity).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PerfilCocineraMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "promedioCalificacion", ignore = true)
    @Mapping(target = "resenasPositivas", ignore = true)
    @Mapping(target = "esDestacada", ignore = true)
    @Mapping(target = "verificada", ignore = true)
    @Mapping(target = "pausada", ignore = true)
    @Mapping(target = "cuentaId", source = "cuentaId")
    @Mapping(target = "nombreCocinera", ignore = true)
    PerfilCocinera toDomain(PerfilCocineraRequestDTO request);

    @Mapping(target = "cuentaId", source = "cuentaId")
    @Mapping(target = "nombreCocinera", source = "nombreCocinera")
    PerfilCocineraResponseDTO toResponseDTO(PerfilCocinera perfil);

    List<PerfilCocineraResponseDTO> toResponseList(List<PerfilCocinera> perfiles);
}