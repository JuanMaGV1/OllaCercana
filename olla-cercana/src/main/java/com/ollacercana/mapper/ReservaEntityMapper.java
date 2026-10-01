package com.ollacercana.mapper;

import com.ollacercana.model.domain.Reserva;
import com.ollacercana.persistence.entity.ReservaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservaEntityMapper {

    ReservaEntity toEntity(Reserva domain);

    Reserva toDomain(ReservaEntity entity);
}