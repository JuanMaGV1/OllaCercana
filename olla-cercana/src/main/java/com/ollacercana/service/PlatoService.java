package com.ollacercana.service;

import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.dto.response.PlatoResponseDTO;

import java.util.UUID;

public interface PlatoService {

    /**
     * HU-04: publica un nuevo plato a nombre de la cocinera autenticada.
     *
     * @param request    datos del plato (sin cocineraId — ver nota de seguridad en PlatoRequestDTO)
     * @param cocineraId id de la cocinera autenticada, resuelto por el controller
     */
    PlatoResponseDTO crear(PlatoRequestDTO request, UUID cocineraId);
}