package com.ollacercana.observer;

import com.ollacercana.dto.response.PorcionesActualizadasResponseDTO;

import com.ollacercana.domain.Plato;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * HU-16: publica PorcionesActualizadasResponseDTO con el mecanismo de eventos internos de Spring,
 * solo cuando las porciones disponibles realmente cambiaron.
 */
@Component
public class PublicadorEventosPorciones {

    private final ApplicationEventPublisher publisher;

    public PublicadorEventosPorciones(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * @param disponiblesAntes porciones disponibles del plato antes de modificarlo
     * @param plato            plato ya modificado
     */
    public void publicarSiCambio(int disponiblesAntes, Plato plato) {
        if (plato.getPorcionesDisponibles() != disponiblesAntes) {
            publisher.publishEvent(PorcionesActualizadasResponseDTO.de(plato));
        }
    }
}
