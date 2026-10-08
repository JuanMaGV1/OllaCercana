package com.ollacercana.core.services;

import java.util.List;
import java.util.UUID;

import com.ollacercana.controller.dtos.request.ConsultaPlatosRequest;
import com.ollacercana.controller.dtos.response.PaginaResponseDTO;
import com.ollacercana.controller.dtos.response.PlatoCercanoResponseDTO;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.TipoAjustePorciones;

public interface PlatoService {

    List<Plato> buscarCercanos(Double latitud, Double longitud);

    Plato crear(Plato plato);

    Plato obtenerPorId(UUID id);

    Plato ajustarDisponibilidad(UUID platoId, TipoAjustePorciones tipo, Integer cantidad, Integer version);

    void eliminar(UUID id);

    PaginaResponseDTO<PlatoCercanoResponseDTO> consultarCercanos(ConsultaPlatosRequest request);
}