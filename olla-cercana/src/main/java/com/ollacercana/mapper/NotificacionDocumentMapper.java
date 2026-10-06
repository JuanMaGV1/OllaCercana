package com.ollacercana.mapper;

import com.ollacercana.model.domain.Notificacion;
import com.ollacercana.persistence.document.NotificacionDocument;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface NotificacionDocumentMapper {
    NotificacionDocument toDocument(Notificacion notificacion);
    Notificacion toDomain(NotificacionDocument doc);
}