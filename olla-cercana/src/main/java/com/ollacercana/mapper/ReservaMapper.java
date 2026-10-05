package com.ollacercana.mapper;

import com.ollacercana.domain.Reserva;
import com.ollacercana.dto.request.ReservaRequestDTO;
import com.ollacercana.dto.response.ReservaResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "compradorId", ignore = true)
    @Mapping(target = "cocineraId", ignore = true)
    @Mapping(target = "cantidadPorciones", source = "cantidad")
    @Mapping(target = "montoTotal", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaLimiteConfirmacion", ignore = true)
    @Mapping(target = "notaComprador", source = "nota")
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
    @Mapping(target = "version", ignore = true)
    Reserva toDomain(ReservaRequestDTO dto);

    @Mapping(target = "monto", source = "reserva.montoTotal")
    @Mapping(target = "montoTotal", source = "reserva.montoTotal")
    @Mapping(target = "horaLimite", source = "reserva.fechaLimiteConfirmacion")
    @Mapping(target = "fechaLimiteConfirmacion", source = "reserva.fechaLimiteConfirmacion")
    @Mapping(target = "plato", source = "nombrePlato")
    @Mapping(target = "conjunto", source = "conjunto")
    ReservaResponseDTO toResponseDTO(Reserva reserva, String nombrePlato, String conjunto);

    @Mapping(target = "monto", source = "montoTotal")
    @Mapping(target = "horaLimite", source = "fechaLimiteConfirmacion")
    @Mapping(target = "plato", ignore = true)
    @Mapping(target = "conjunto", ignore = true)
    ReservaResponseDTO toResponse(Reserva reserva);

    List<ReservaResponseDTO> toResponseList(List<Reserva> reservas);
}