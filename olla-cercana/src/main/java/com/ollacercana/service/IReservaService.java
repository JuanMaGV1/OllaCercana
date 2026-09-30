package com.ollacercana.service;

import com.ollacercana.model.domain.Reserva;
import com.ollacercana.model.dto.response.ReservaResponseDTO;

public interface IReservaService {
    ReservaResponseDTO crear(Long compradorId, Reserva reserva);
}