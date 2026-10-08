package com.ollacercana.core.patterns.observer;

import com.ollacercana.core.models.EventoReserva;
import com.ollacercana.core.models.enums.TipoEvento;
import com.ollacercana.core.services.MedallaService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * HU-21 / OC-279: al completarse una entrega evalúa si el comprador gana la insignia Vecino Fiel.
 */
@Component
@RequiredArgsConstructor
public class MedallaObservador implements ObservadorReserva {

    private static final Logger log = LoggerFactory.getLogger(MedallaObservador.class);

    private final MedallaService medallaService;

    @Override
    public void notificar(EventoReserva evento) {
        if (evento.tipo() != TipoEvento.RESERVA_COMPLETADA) {
            return;
        }
        try {
            medallaService.evaluarVecinoFiel(evento.compradorId(), evento.cocineraId(), LocalDateTime.now());
        } catch (RuntimeException e) {
            log.warn("No se pudo evaluar la insignia Vecino Fiel de la reserva {}: {}",
                    evento.reservaId(), e.getMessage());
        }
    }
}
