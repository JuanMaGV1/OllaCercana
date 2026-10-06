package com.ollacercana.service;

import com.ollacercana.dto.request.MapaCocinerasRequestDTO;
import com.ollacercana.dto.response.CocineraMapaResponseDTO;

import java.util.List;

public interface CocineraMapaService {

    /**
     * OC-232 / OC-233: Consulta cocineras con oferta activa (plato activo, no expirado, porciones > 0)
     * dentro del radio indicado, retornando coordenadas ofuscadas y distancia redondeada a múltiplos de 100m.
     */
    List<CocineraMapaResponseDTO> buscarCocinerasEnMapa(MapaCocinerasRequestDTO request);
}
