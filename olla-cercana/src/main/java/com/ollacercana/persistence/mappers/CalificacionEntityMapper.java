package com.ollacercana.persistence.mappers;

import com.ollacercana.core.models.Calificacion;
import com.ollacercana.persistence.entities.CalificacionEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OC-193: mapper entre el POJO de dominio Calificacion y su entidad JPA.
 */
@Component
public class CalificacionEntityMapper {

    public CalificacionEntity toEntity(Calificacion c) {
        if (c == null) return null;
        return CalificacionEntity.builder()
                .id(c.getId())
                .reservaId(c.getReservaId())
                .compradorId(c.getCompradorId())
                .cocineraId(c.getCocineraId())
                .estrellas(c.getEstrellas())
                .comentario(c.getComentario())
                .fechaCreacion(c.getFechaCreacion())
                .build();
    }

    public Calificacion toDomain(CalificacionEntity e) {
        if (e == null) return null;
        return Calificacion.builder()
                .id(e.getId())
                .reservaId(e.getReservaId())
                .compradorId(e.getCompradorId())
                .cocineraId(e.getCocineraId())
                .estrellas(e.getEstrellas())
                .comentario(e.getComentario())
                .fechaCreacion(e.getFechaCreacion())
                .build();
    }

    public List<Calificacion> toDomainList(List<CalificacionEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toDomain).toList();
    }
}