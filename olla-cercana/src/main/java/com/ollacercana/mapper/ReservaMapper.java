package com.ollacercana.mapper;

import com.ollacercana.domain.Reserva;
import com.ollacercana.dto.response.ReservaResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservaMapper {

    ReservaResponseDTO toResponse(Reserva reserva);

    List<ReservaResponseDTO> toResponseList(List<Reserva> reservas);
}
