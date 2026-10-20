package com.ollacercana.core.patterns.observer;

import com.ollacercana.core.models.EventoReserva;
import com.ollacercana.persistence.mappers.EventoMapper;
import com.ollacercana.persistence.repository.mongo.EventoReservaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditoriaEventoObservador implements ObservadorReserva {

    private final EventoReservaRepository eventoReservaRepository;
    private final EventoMapper eventoMapper;

    @Override
    public void notificar(EventoReserva evento) {
        eventoReservaRepository.save(eventoMapper.toDocument(evento));
        log.info("Auditoría de evento registrada: tipo={}, reservaId={}", evento.tipo(), evento.reservaId());
    }
}