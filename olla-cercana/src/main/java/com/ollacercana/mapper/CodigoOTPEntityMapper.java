package com.ollacercana.mapper;

import com.ollacercana.model.domain.CodigoOTP;
import com.ollacercana.persistence.entity.CodigoOTPEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CodigoOTPEntityMapper {
    CodigoOTPEntity toEntity(CodigoOTP otp);
    CodigoOTP toDomain(CodigoOTPEntity entity);
}