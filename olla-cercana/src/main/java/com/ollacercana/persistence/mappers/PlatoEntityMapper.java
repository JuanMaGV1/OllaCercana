package com.ollacercana.persistence.mappers;

import com.ollacercana.core.models.Plato;
import com.ollacercana.persistence.entities.PlatoEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlatoEntityMapper {

    public PlatoEntity toEntity(Plato p) {
        if (p == null) return null;
        return PlatoEntity.builder()
                .id(p.getId())
                .cocineraId(p.getCocineraId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .fotoUrl(p.getFotoUrl())
                .tipoComida(p.getTipoComida())
                .restricciones(p.getRestricciones())
                .porcionesTotales(p.getPorcionesTotales())
                .porcionesComprometidas(p.getPorcionesComprometidas())
                .precioPorcion(p.getPrecioPorcion())
                .estado(p.getEstado())
                .horaDisponibilidad(p.getHoraDisponibilidad())
                .fechaPublicacion(p.getFechaPublicacion())
                .fechaExpiracion(p.getFechaExpiracion())
                .latitud(p.getLatitud())
                .longitud(p.getLongitud())
                .puntoEntrega(p.getPuntoEntrega())
                .version(p.getVersion())
                .build();
    }

    public Plato toDomain(PlatoEntity e) {
        if (e == null) return null;
        return Plato.builder()
                .id(e.getId())
                .cocineraId(e.getCocineraId()) 
                .nombre(e.getNombre())
                .descripcion(e.getDescripcion())
                .fotoUrl(e.getFotoUrl())
                .tipoComida(e.getTipoComida())
                .restricciones(e.getRestricciones())
                .porcionesTotales(e.getPorcionesTotales())
                .porcionesComprometidas(e.getPorcionesComprometidas())
                .precioPorcion(e.getPrecioPorcion())
                .estado(e.getEstado())
                .horaDisponibilidad(e.getHoraDisponibilidad())
                .fechaPublicacion(e.getFechaPublicacion())
                .fechaExpiracion(e.getFechaExpiracion())
                .latitud(e.getLatitud())
                .longitud(e.getLongitud())
                .puntoEntrega(e.getPuntoEntrega())
                .version(e.getVersion())
                .build();
    }

    public List<Plato> toDomainList(List<PlatoEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }
}