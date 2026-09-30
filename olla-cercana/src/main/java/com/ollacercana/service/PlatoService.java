package com.ollacercana.service;

import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.model.dto.response.PlatoCercanoResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PlatoService {

    List<PlatoCercanoResponseDTO> buscarCercanos(Double latitud, Double longitud);
    /**
     * OC-92 / HU-04: publica un nuevo plato. Recibe y devuelve el dominio;
     */
    Plato crear(Plato plato);

    /**
     * Consulta un plato por id.
     */
    Plato obtenerPorId(UUID id);

    /**
     * HU-24: ajusta manualmente la disponibilidad de un plato (aumentar, disminuir, marcar agotado).
     */
    Plato ajustarDisponibilidad(UUID platoId, AjusteDisponibilidadRequest request);

    /**
     * Elimina un plato por id .
     */
    void eliminar(UUID id);
}
