package com.ollacercana.service;

import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.exception.BusinessRuleException;
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
                .orElseThrow(() -> new BusinessRuleException("Cocinera no encontrada: " + cocineraId));
    }

    @Override
    public boolean estaPausada(UUID cocineraId) {
        return repository.findById(cocineraId)
                .map(PerfilCocinera::pausada)
                .orElseThrow(() -> new BusinessRuleException("Cocinera no encontrada: " + cocineraId));
    }
}