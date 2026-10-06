package com.ollacercana.mapper;

import com.ollacercana.model.domain.Cuenta;
import com.ollacercana.persistence.entity.CuentaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CuentaEntityMapper {
    CuentaEntity toEntity(Cuenta cuenta);
    Cuenta toDomain(CuentaEntity entity);
    List<Cuenta> toDomainList(List<CuentaEntity> entities);
}