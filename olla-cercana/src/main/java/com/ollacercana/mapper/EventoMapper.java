package com.ollacercana.mapper;

import com.ollacercana.model.domain.EventoReserva;
import com.ollacercana.persistence.document.EventoReservaDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EventoMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "payloadJson", expression = "java(evento.getPayload() != null ? evento.getPayload().toString() : null)")
    EventoReservaDocument toDocument(EventoReserva evento);

    EventoReserva toDomain(EventoReservaDocument doc);
}