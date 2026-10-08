package com.ollacercana.persistence.mappers;

import com.ollacercana.core.models.Reserva;
import com.ollacercana.persistence.entities.ReservaEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ReservaEntityMapper {

    @Mapping(target = "calificacion", source = "calificacion")
    ReservaEntity toEntity(Reserva reserva);

    @Mapping(target = "calificacion", source = "calificacion")
    Reserva toDomain(ReservaEntity entity);

    List<Reserva> toDomainList(List<ReservaEntity> entities);
}