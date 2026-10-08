package com.ollacercana.persistence.mappers;

import com.ollacercana.core.models.Medalla;
import com.ollacercana.persistence.entities.MedallaEntity;
import org.springframework.stereotype.Component;

@Component
public class MedallaEntityMapper {

    public MedallaEntity toEntity(Medalla m) {
        if (m == null) return null;
        return MedallaEntity.builder()
                .codigo(m.getCodigo())
                .nombre(m.getNombre())
                .requisito(m.getRequisito())
                .build();
    }

    public Medalla toDomain(MedallaEntity e) {
        if (e == null) return null;
        return Medalla.builder()
                .codigo(e.getCodigo())
                .nombre(e.getNombre())
                .requisito(e.getRequisito())
                .build();
    }
}