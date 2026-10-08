package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.response.CocineraMapaResponseDTO;

import java.util.List;

public interface CocineraMapaService {
    List<CocineraMapaResponseDTO> buscarCocinerasEnMapa(Double latitud, Double longitud, Double radio);
}