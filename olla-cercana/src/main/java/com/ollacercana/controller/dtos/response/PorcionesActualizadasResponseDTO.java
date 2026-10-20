package com.ollacercana.controller.dtos.response;

import java.util.UUID;

import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.EstadoPorciones;

/**
 * HU-16: evento interno que indica que cambió el stock de un plato.
 */
public record PorcionesActualizadasResponseDTO(UUID platoId, int porcionesDisponibles, EstadoPorciones estado) {

    public static PorcionesActualizadasResponseDTO de(Plato plato) {
        int disponibles = plato.getPorcionesDisponibles();
        EstadoPorciones estado = plato.getEstado() == EstadoPlato.ACTIVO && disponibles > 0
                ? EstadoPorciones.DISPONIBLE
                : EstadoPorciones.AGOTADO;
        return new PorcionesActualizadasResponseDTO(plato.getId(), disponibles, estado);
    }
}
