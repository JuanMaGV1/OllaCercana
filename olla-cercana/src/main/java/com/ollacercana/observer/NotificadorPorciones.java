package com.ollacercana.observer;

import com.ollacercana.dto.response.PorcionesActualizadasResponseDTO;

/** HU-16: entrega una actualización de porciones a los suscriptores del plato. */
public interface NotificadorPorciones {
    void notificar(PorcionesActualizadasResponseDTO evento);
}
