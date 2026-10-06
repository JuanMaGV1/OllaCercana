package com.ollacercana.observer;

import com.ollacercana.model.domain.EventoReserva;

public interface ObservadorReserva {

    void notificar(EventoReserva evento);
}