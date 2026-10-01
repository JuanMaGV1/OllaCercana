package com.ollacercana.mapper;

import com.ollacercana.model.domain.CodigoOTP;
import com.ollacercana.persistence.entity.CodigoOTPEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CodigoOTPEntityMapper {

    CodigoOTPEntity toEntity(CodigoOTP domain);

    CodigoOTP toDomain(CodigoOTPEntity entity);
}