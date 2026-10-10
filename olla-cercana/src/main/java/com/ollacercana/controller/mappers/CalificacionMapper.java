package com.ollacercana.controller.mappers;

import com.ollacercana.controller.dtos.request.CalificacionRequestDTO;
import com.ollacercana.controller.dtos.response.CalificacionResponseDTO;
import com.ollacercana.core.models.Calificacion;
import org.springframework.stereotype.Component;

/**
 * OC-193: convierte entre DTOs de calificación y el dominio.
 */
@Component
public class CalificacionMapper {

    public Calificacion toDomain(CalificacionRequestDTO request) {
        if (request == null) return null;
        return Calificacion.builder()
                .estrellas(request.getEstrellas())
                .comentario(request.getComentario())
                .build();
    }

    public CalificacionResponseDTO toResponse(Calificacion c) {
        if (c == null) return null;
        return CalificacionResponseDTO.builder()
                .id(c.getId())
                .reservaId(c.getReservaId())
                .cocineraId(c.getCocineraId())
                .compradorId(c.getCompradorId())
                .estrellas(c.getEstrellas())
                .comentario(c.getComentario())
                .estado(c.getEstado())
                .fechaCreacion(c.getFechaCreacion())
                .fechaPublicacion(c.getFechaPublicacion())
                .build();
    }
}