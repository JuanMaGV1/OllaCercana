package com.ollacercana.observer;

import com.ollacercana.domain.EventoReserva;
import com.ollacercana.repository.mongo.EventoReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditoriaEventoObservador implements ObservadorReserva {

    private final EventoReservaRepository eventoReservaRepository;

    @Override
    public void notificar(EventoReserva evento) {
        eventoReservaRepository.save(evento);
        log.info("Auditoría de evento registrada: tipo={}, reservaId={}", evento.tipo(), evento.reservaId());
    }
}