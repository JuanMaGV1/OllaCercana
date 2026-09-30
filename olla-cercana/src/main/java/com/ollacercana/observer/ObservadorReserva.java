package com.ollacercana.observer;

import com.ollacercana.domain.EventoReserva;

public interface ObservadorReserva {

    void notificar(EventoReserva evento);
}