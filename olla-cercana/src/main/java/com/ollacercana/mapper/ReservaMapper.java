package com.ollacercana.mapper;

import com.ollacercana.model.domain.Reserva;
import com.ollacercana.model.dto.request.ReservaRequestDTO;
import com.ollacercana.model.dto.response.ReservaResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cocineraId", ignore = true)
    @Mapping(target = "compradorId", ignore = true)
    @Mapping(target = "montoTotal", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaLimiteConfirmacion", ignore = true)
    @Mapping(target = "fechaDecision", ignore = true)
    @Mapping(target = "horaEstimadaEntrega", ignore = true)
    @Mapping(target = "motivoRechazo", ignore = true)
    @Mapping(target = "comentarioRechazo", ignore = true)
    @Mapping(target = "recordatorioEnviado", ignore = true)
    @Mapping(target = "chatHabilitado", ignore = true)
    @Mapping(target = "estadoChat", ignore = true)
    @Mapping(target = "fechaCompletada", ignore = true)
    @Mapping(target = "comentarioCierre", ignore = true)
    @Mapping(target = "calificacionHabilitada", ignore = true)
    @Mapping(target = "cantidadPorciones", source = "cantidad")
    @Mapping(target = "notaComprador", source = "nota")
    Reserva toDomain(ReservaRequestDTO dto);

    ReservaResponseDTO toResponse(Reserva reserva);

    List<ReservaResponseDTO> toResponseList(List<Reserva> reservas);
}