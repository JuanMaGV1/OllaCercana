package com.ollacercana.core.patterns.observer;

import com.ollacercana.controller.dtos.response.PorcionesActualizadasResponseDTO;
import com.ollacercana.core.models.Plato;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

   
                                                                                                  
                                                             
   
@Component
public class PublicadorEventosPorciones {

    private final ApplicationEventPublisher publisher;

    public PublicadorEventosPorciones(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

       
                                                                                   
                                                  
       
    public void publicarSiCambio(int disponiblesAntes, Plato plato) {
        if (plato.getPorcionesDisponibles() != disponiblesAntes) {
            publisher.publishEvent(PorcionesActualizadasResponseDTO.de(plato));
        }
    }
}
