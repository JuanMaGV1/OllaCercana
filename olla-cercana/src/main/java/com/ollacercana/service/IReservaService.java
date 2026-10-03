package com.ollacercana.service;

import com.ollacercana.domain.Reserva;

public interface IReservaService {
    Reserva crear(Long compradorId, Reserva reserva);
}