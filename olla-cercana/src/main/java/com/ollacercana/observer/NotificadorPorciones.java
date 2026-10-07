package com.ollacercana.observer;

import com.ollacercana.dto.response.PorcionesActualizadasResponseDTO;

                                                                                  
public interface NotificadorPorciones {
    void notificar(PorcionesActualizadasResponseDTO evento);
}
