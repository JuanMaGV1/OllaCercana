package com.ollacercana.service;

import com.ollacercana.domain.Reserva;
import com.ollacercana.dto.response.ReservaResponseDTO;

public interface IReservaService {
    ReservaResponseDTO crear(Long compradorId, Reserva reserva);
}