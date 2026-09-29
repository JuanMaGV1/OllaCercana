package com.ollacercana.mapper;

import com.ollacercana.domain.Reserva;
import com.ollacercana.dto.request.ReservaRequestDTO;
import com.ollacercana.dto.response.ReservaResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "compradorId", ignore = true)
    @Mapping(target = "cantidadPorciones", source = "cantidad")
    @Mapping(target = "montoTotal", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaLimiteConfirmacion", ignore = true)
    @Mapping(target = "notaComprador", source = "nota")
    Reserva toDomain(ReservaRequestDTO dto);

    @Mapping(target = "monto", source = "reserva.montoTotal")
    @Mapping(target = "horaLimite", source = "reserva.fechaLimiteConfirmacion")
    @Mapping(target = "plato", source = "nombrePlato")
    @Mapping(target = "conjunto", source = "conjunto")
    ReservaResponseDTO toResponseDTO(Reserva reserva, String nombrePlato, String conjunto);
}