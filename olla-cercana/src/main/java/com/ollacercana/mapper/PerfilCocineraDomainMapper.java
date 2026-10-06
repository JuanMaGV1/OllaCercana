package com.ollacercana.mapper;

import com.ollacercana.model.domain.PerfilCocinera;
import com.ollacercana.persistence.entity.PerfilCocineraEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper Entity → Dominio. Dirección única para evitar colisión de builders en MapStruct.
 * Lo usa el ServiceImpl para traducir lo que viene de BD.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PerfilCocineraDomainMapper {

    @Mapping(target = "cuentaId", source = "cuenta.id")
    @Mapping(target = "nombreCocinera", source = "cuenta.identidad.nombre")
    PerfilCocinera toDomain(PerfilCocineraEntity entity);

    List<PerfilCocinera> toDomainList(List<PerfilCocineraEntity> entities);
}