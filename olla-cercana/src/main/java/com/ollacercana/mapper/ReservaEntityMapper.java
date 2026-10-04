package com.ollacercana.mapper;

import com.ollacercana.domain.Reserva;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservaEntityMapper {

    Reserva toEntity(Reserva domain);

    Reserva toDomain(Reserva entity);
}