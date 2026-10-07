package com.ollacercana.observer;

import com.ollacercana.dto.response.PorcionesActualizadasResponseDTO;

import java.io.IOException;

   
                                                           
   
public interface ObservadorPorciones {
    void enviar(PorcionesActualizadasResponseDTO evento) throws IOException;
}
