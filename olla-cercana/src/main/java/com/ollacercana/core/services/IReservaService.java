package com.ollacercana.core.services;

import com.ollacercana.core.models.Reserva;

public interface IReservaService {
    Reserva crear(Long compradorId, Reserva reserva);
}