package com.ollacercana.core.patterns.observer;

import com.ollacercana.core.models.EventoReserva;

public interface ObservadorReserva {

    void notificar(EventoReserva evento);
}