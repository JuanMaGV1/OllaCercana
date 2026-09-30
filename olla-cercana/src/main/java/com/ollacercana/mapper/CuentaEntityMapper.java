package com.ollacercana.mapper;

import org.mapstruct.Mapper;

import com.ollacercana.model.domain.Cuenta;

@Mapper(componentModel = "spring")
public interface CuentaEntityMapper {

    Cuenta toEntity(Cuenta domain);

    Cuenta toDomain(Cuenta entity);
}