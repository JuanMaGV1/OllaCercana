package com.ollacercana.service;

import com.ollacercana.dto.response.HistorialPaginadoResponseDTO;
import com.ollacercana.dto.response.MetricasCocineraResponseDTO;

import java.time.LocalDate;
import java.util.UUID;

public interface MetricasCocineraService {

    /**
     * Ingresos referenciales, porciones entregadas y plato más pedido de la cocinera.
     * Desde y hasta son opcionales; sin periodo devuelve el acumulado.
     */
    MetricasCocineraResponseDTO obtenerMetricas(UUID cocineraId, LocalDate desde, LocalDate hasta);

    /**
     * Historial paginado de pedidos COMPLETADOS, del más reciente al más antiguo.
     */
    HistorialPaginadoResponseDTO obtenerHistorial(UUID cocineraId, int pagina, int tamanio);
}
