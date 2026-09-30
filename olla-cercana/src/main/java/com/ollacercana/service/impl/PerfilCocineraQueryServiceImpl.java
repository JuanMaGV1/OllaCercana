package com.ollacercana.service.impl;

import com.ollacercana.exception.CocineraNoEncontradaException;
import com.ollacercana.model.domain.PerfilCocinera;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.validator.CocineraQueryPort;
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
                .map(PerfilCocinera::verificada)
                .orElseThrow(() -> new CocineraNoEncontradaException(cocineraId));
    }

    @Override
    public boolean estaPausada(UUID cocineraId) {
        return repository.findById(cocineraId)
                .map(PerfilCocinera::pausada)
                .orElseThrow(() -> new CocineraNoEncontradaException(cocineraId));
    }
}