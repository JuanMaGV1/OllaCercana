package com.ollacercana.mapper;

import com.ollacercana.model.domain.PerfilCocinera;
import com.ollacercana.persistence.entity.PerfilCocineraEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper Dominio → Entity. Dirección única para evitar colisión de builders en MapStruct.
 * Lo usa el ServiceImpl cuando va a guardar en BD.
 * La propiedad "cuenta" se ignora aquí porque el Service la inyecta manualmente.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PerfilCocineraPersistenceMapper {

    @Mapping(target = "cuenta", ignore = true)
    PerfilCocineraEntity toEntity(PerfilCocinera perfil);
}