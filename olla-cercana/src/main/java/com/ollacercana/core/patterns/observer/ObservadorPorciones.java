package com.ollacercana.core.patterns.observer;

import java.io.IOException;

import com.ollacercana.controller.dtos.response.PorcionesActualizadasResponseDTO;

/**
 * Destino de las actualizaciones de porciones de un plato.
 */
public interface ObservadorPorciones {
    void enviar(PorcionesActualizadasResponseDTO evento) throws IOException;
}
