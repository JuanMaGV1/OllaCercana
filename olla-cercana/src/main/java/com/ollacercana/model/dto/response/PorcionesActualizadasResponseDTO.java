package com.ollacercana.model.dto.response;

import java.util.UUID;

import com.ollacercana.model.domain.EstadoPlato;
import com.ollacercana.model.domain.EstadoPorciones;
import com.ollacercana.model.domain.Plato;

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
