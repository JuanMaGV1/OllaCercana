package com.ollacercana.persistence.mappers;

import com.ollacercana.core.models.CodigoOTP;
import com.ollacercana.persistence.entities.CodigoOTPEntity;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CodigoOTPEntityMapper {
    CodigoOTPEntity toEntity(CodigoOTP otp);
    CodigoOTP toDomain(CodigoOTPEntity entity);
}