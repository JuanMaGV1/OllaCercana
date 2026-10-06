package com.ollacercana.mapper;

import com.ollacercana.model.domain.Reserva;
import com.ollacercana.persistence.entity.ReservaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ReservaEntityMapper {
    ReservaEntity toEntity(Reserva reserva);
    Reserva toDomain(ReservaEntity entity);
    List<Reserva> toDomainList(List<ReservaEntity> entities);
}