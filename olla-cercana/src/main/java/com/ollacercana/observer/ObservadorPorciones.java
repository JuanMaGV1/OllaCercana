package com.ollacercana.observer;

import java.io.IOException;

import com.ollacercana.model.dto.response.PorcionesActualizadasResponseDTO;

/**
 * Destino de las actualizaciones de porciones de un plato.
 */
public interface ObservadorPorciones {
    void enviar(PorcionesActualizadasResponseDTO evento) throws IOException;
}
