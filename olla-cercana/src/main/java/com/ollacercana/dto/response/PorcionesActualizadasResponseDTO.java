package com.ollacercana.dto.response;

import com.ollacercana.domain.EstadoPorciones;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.Plato;

import java.util.UUID;

   
                                                                    
   
public record PorcionesActualizadasResponseDTO(UUID platoId, int porcionesDisponibles, EstadoPorciones estado) {

    public static PorcionesActualizadasResponseDTO de(Plato plato) {
        int disponibles = plato.getPorcionesDisponibles();
        EstadoPorciones estado = plato.getEstado() == EstadoPlato.ACTIVO && disponibles > 0
                ? EstadoPorciones.DISPONIBLE
                : EstadoPorciones.AGOTADO;
        return new PorcionesActualizadasResponseDTO(plato.getId(), disponibles, estado);
    }
}
