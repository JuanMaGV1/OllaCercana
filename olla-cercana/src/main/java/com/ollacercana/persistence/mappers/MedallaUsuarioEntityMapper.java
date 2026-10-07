package com.ollacercana.persistence.mappers;

import com.ollacercana.core.models.MedallaUsuario;
import com.ollacercana.persistence.entities.MedallaUsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class MedallaUsuarioEntityMapper {

    private final MedallaEntityMapper medallaMapper;

    public MedallaUsuarioEntityMapper(MedallaEntityMapper medallaMapper) {
        this.medallaMapper = medallaMapper;
    }

    public MedallaUsuarioEntity toEntity(MedallaUsuario m) {
        if (m == null) return null;
        return MedallaUsuarioEntity.builder()
                .id(m.getId())
                .usuarioId(m.getUsuarioId())
                .medalla(m.getMedalla() == null ? null : medallaMapper.toEntity(m.getMedalla()))
                .fechaOtorgada(m.getFechaOtorgada())
                .vigenteHasta(m.getVigenteHasta())
                .build();
    }

    public MedallaUsuario toDomain(MedallaUsuarioEntity e) {
        if (e == null) return null;
        return MedallaUsuario.builder()
                .id(e.getId())
                .usuarioId(e.getUsuarioId())
                .medalla(medallaMapper.toDomain(e.getMedalla()))
                .fechaOtorgada(e.getFechaOtorgada())
                .vigenteHasta(e.getVigenteHasta())
                .build();
    }
}