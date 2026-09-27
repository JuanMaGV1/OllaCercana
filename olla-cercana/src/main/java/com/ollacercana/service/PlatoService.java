package com.ollacercana.service;

import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.dto.response.PlatoResponseDTO;
import com.ollacercana.dto.request.AjusteDisponibilidadRequest;

import java.util.UUID;

public interface PlatoService {

    /**
     * HU-04: publica un nuevo plato a nombre de la cocinera autenticada.
     *
     * @param request    datos del plato
     * @param cocineraId id de la cocinera autenticada, resuelto por el controller
     */
    PlatoResponseDTO crear(PlatoRequestDTO request, UUID cocineraId);

    /**
     * HU-24: ajusta manualmente la disponibilidad de un plato ya sea aumentar, disminuir, marcar agotado.
     */
    PlatoResponseDTO ajustarDisponibilidad(UUID platoId, AjusteDisponibilidadRequest request);
}