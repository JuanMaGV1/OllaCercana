package com.ollacercana.service;

import com.ollacercana.domain.TipoComida;
import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.dto.response.PlatoResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PlatoService {

    PlatoResponseDTO publicar(PlatoRequestDTO request);

    PlatoResponseDTO obtenerPorId(UUID id);

    List<PlatoResponseDTO> listarActivos();

    List<PlatoResponseDTO> listarPorTipo(TipoComida tipo);

    PlatoResponseDTO ajustarPorciones(UUID id, int nuevaCantidad);

    void eliminar(UUID id);
}