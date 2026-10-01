package com.ollacercana.service.impl;

import com.ollacercana.exception.CocineraNoEncontradaException;
import com.ollacercana.mapper.PerfilCocineraEntityMapper;
import com.ollacercana.model.domain.PerfilCocinera;
import com.ollacercana.persistence.entity.PerfilCocineraEntity;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.validator.CocineraQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerfilCocineraQueryServiceImpl implements CocineraQueryPort {

    private final PerfilCocineraRepository repository;
    private final PerfilCocineraEntityMapper mapper;

    @Override
    public boolean estaVerificada(UUID cocineraId) {
        PerfilCocineraEntity entity = repository.findById(cocineraId)
                .orElseThrow(() -> new CocineraNoEncontradaException(cocineraId));
        PerfilCocinera perfil = mapper.toDomain(entity);
        return perfil.isVerificada();
    }

    @Override
    public boolean estaPausada(UUID cocineraId) {
        PerfilCocineraEntity entity = repository.findById(cocineraId)
                .orElseThrow(() -> new CocineraNoEncontradaException(cocineraId));
        PerfilCocinera perfil = mapper.toDomain(entity);
        return perfil.isPausada();
    }
}