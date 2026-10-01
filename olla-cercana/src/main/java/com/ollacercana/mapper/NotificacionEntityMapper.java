package com.ollacercana.mapper;

import com.ollacercana.model.domain.Notificacion;
import com.ollacercana.persistence.entity.NotificacionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificacionEntityMapper {

    NotificacionEntity toEntity(Notificacion domain);

    Notificacion toDomain(NotificacionEntity entity);
}