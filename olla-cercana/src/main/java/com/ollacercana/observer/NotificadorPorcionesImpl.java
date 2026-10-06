package com.ollacercana.observer;

import com.ollacercana.dto.response.PorcionesActualizadasResponseDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NotificadorPorcionesImpl implements NotificadorPorciones {

    private static final Logger log = LoggerFactory.getLogger(NotificadorPorcionesImpl.class);

    private final RegistroSuscripciones registro;

    public NotificadorPorcionesImpl(RegistroSuscripciones registro) {
        this.registro = registro;
    }

    @Override
    public void notificar(PorcionesActualizadasResponseDTO evento) {
        for (ObservadorPorciones suscriptor : registro.de(evento.platoId())) {
            try {
                suscriptor.enviar(evento);
            } catch (IOException | RuntimeException e) {
                log.debug("Suscriptor caído, se elimina del plato {}", evento.platoId());
                registro.cancelar(evento.platoId(), suscriptor);
            }
        }
    }
}
