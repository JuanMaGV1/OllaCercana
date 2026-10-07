package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.response.HistorialPaginadoResponseDTO;
import com.ollacercana.controller.dtos.response.MetricasCocineraResponseDTO;

import java.time.LocalDate;
import java.util.UUID;

public interface MetricasCocineraService {

    MetricasCocineraResponseDTO obtenerMetricas(UUID cocineraId, LocalDate desde, LocalDate hasta);

    HistorialPaginadoResponseDTO obtenerHistorial(UUID cocineraId, int pagina, int tamanio);
}
