package com.ollacercana.core.services.impl;

import com.ollacercana.controller.handlers.exception.CocineraNoEncontradaException;
import com.ollacercana.core.validators.CocineraQueryPort;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerfilCocineraQueryServiceImpl implements CocineraQueryPort {

    private final PerfilCocineraRepository repository;

    @Override
    public boolean estaVerificada(UUID cocineraId) {
        return repository.findById(cocineraId)
                .map(entity -> entity.isVerificada())
                .orElseThrow(() -> new CocineraNoEncontradaException(cocineraId));
    }

    @Override
    public boolean estaPausada(UUID cocineraId) {
        return repository.findById(cocineraId)
                .map(entity -> entity.isPausada())
                .orElseThrow(() -> new CocineraNoEncontradaException(cocineraId));
    }
}