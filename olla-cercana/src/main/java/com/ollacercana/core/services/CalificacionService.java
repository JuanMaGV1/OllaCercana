package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.request.CalificacionRequestDTO;
import com.ollacercana.controller.dtos.response.CalificacionResponseDTO;
import com.ollacercana.controller.dtos.response.PaginaResponseDTO;
import com.ollacercana.controller.dtos.response.ResumenCalificacionesDTO;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * HU-31: casos de uso de calificaciones de reservas.
 */
public interface CalificacionService {

    /** Publica una calificación sobre una reserva COMPLETADA. */
    CalificacionResponseDTO calificar(UUID reservaId, Long compradorId, CalificacionRequestDTO request);

    /** Lista paginada de calificaciones de una cocinera (opcionalmente filtradas por estrellas). */
    PaginaResponseDTO<CalificacionResponseDTO> listarPorCocinera(UUID cocineraId, Integer estrellas, int page, int size);

    /** Resumen agregado (promedio, total, positivas). */
    ResumenCalificacionesDTO obtenerResumen(UUID cocineraId);

    /** OC-196: publica las calificaciones PENDIENTES cuya ventana de 72 h ya venció. */
    int publicarPendientesVencidas(LocalDateTime ahora);
}