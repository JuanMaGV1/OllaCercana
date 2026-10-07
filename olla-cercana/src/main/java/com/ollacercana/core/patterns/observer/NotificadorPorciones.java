package com.ollacercana.core.patterns.observer;

import com.ollacercana.controller.dtos.response.PorcionesActualizadasResponseDTO;

/** HU-16: entrega una actualización de porciones a los suscriptores del plato. */
public interface NotificadorPorciones {
    void notificar(PorcionesActualizadasResponseDTO evento);
}
