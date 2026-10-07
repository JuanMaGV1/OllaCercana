package com.ollacercana.observer;

import com.ollacercana.dto.response.PorcionesActualizadasResponseDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

   
                                                                                
   
@Component
public class PorcionesActualizadasObservador {

    private static final Logger log = LoggerFactory.getLogger(PorcionesActualizadasObservador.class);

    private final NotificadorPorciones notificador;

    public PorcionesActualizadasObservador(NotificadorPorciones notificador) {
        this.notificador = notificador;
    }

    @EventListener
    public void alActualizar(PorcionesActualizadasResponseDTO evento) {
        log.info("Porciones actualizadas: plato={}, disponibles={}, estado={}",
                evento.platoId(), evento.porcionesDisponibles(), evento.estado());
        notificador.notificar(evento);
    }
}
