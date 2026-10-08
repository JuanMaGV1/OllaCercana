package com.ollacercana.observer;

import com.ollacercana.dto.response.PorcionesActualizadasResponseDTO;

import com.ollacercana.domain.Plato;
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
