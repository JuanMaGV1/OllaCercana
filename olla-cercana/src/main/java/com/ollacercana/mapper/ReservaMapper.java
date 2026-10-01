package com.ollacercana.mapper;

import com.ollacercana.model.dto.response.ReservaResponseDTO;
import com.ollacercana.model.domain.Reserva;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservaMapper {

    ReservaResponseDTO toResponse(Reserva reserva);

    List<ReservaResponseDTO> toResponseList(List<Reserva> reservas);
}