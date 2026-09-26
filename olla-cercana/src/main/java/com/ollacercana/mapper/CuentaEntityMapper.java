package com.ollacercana.mapper;

import com.ollacercana.domain.Cuenta;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CuentaEntityMapper {

    Cuenta toEntity(Cuenta domain);

    Cuenta toDomain(Cuenta entity);
}