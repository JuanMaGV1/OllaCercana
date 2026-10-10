package com.ollacercana.core.patterns.observer;

import com.ollacercana.controller.dtos.response.PorcionesActualizadasResponseDTO;

                                                                                  
public interface NotificadorPorciones {
    
    void notificar(PorcionesActualizadasResponseDTO evento);
    
}
