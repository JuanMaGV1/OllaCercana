package com.ollacercana.core.patterns.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ollacercana.core.models.EventoReserva;

import java.util.List;

   
                             
   

@Component
public class PublicadorEventosReserva {

    private static final Logger log = LoggerFactory.getLogger(PublicadorEventosReserva.class);

    private final List<com.ollacercana.core.patterns.observer.ObservadorReserva> observadores;

    public PublicadorEventosReserva(List<com.ollacercana.core.patterns.observer.ObservadorReserva> observadores) {
        this.observadores = List.copyOf(observadores);
    }

    public void publicar(EventoReserva evento) {
        for (com.ollacercana.core.patterns.observer.ObservadorReserva observador : observadores) {
            try {
                observador.notificar(evento);
            } catch (RuntimeException e) {
                log.error("El observador {} falló procesando el evento {} de la reserva {}: {}",
                        observador.getClass().getSimpleName(), evento.tipo(), evento.reservaId(), e.getMessage());
            }
        }
    }
}
