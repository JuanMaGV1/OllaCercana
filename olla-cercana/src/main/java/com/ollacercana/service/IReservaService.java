package com.ollacercana.service;

import com.ollacercana.model.domain.Reserva;

public interface IReservaService {
    Reserva crear(Long compradorId, Reserva reserva);
}