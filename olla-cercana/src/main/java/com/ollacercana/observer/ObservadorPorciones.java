package com.ollacercana.observer;

import com.ollacercana.dto.response.PorcionesActualizadasResponseDTO;

import java.io.IOException;

/**
 * Destino de las actualizaciones de porciones de un plato.
 */
public interface ObservadorPorciones {
    void enviar(PorcionesActualizadasResponseDTO evento) throws IOException;
}
