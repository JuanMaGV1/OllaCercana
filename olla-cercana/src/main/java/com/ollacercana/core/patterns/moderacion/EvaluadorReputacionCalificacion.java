package com.ollacercana.core.patterns.moderacion;

import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.repository.CalificacionRepository;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EvaluadorReputacionCalificacion {

    public static final double PROMEDIO_MINIMO = 3.5;
    public static final int RESENAS_MINIMAS = 5;

    private final CalificacionRepository calificacionRepository;
    private final PerfilCocineraRepository perfilRepository;

    public void evaluar(UUID cocineraId) {
        Long total = calificacionRepository.contarTotal(cocineraId);
        if (total == null || total < RESENAS_MINIMAS) return;

        Double promedio = calificacionRepository.promedioPorCocinera(cocineraId);
        if (promedio == null) return;

        perfilRepository.findById(cocineraId).ifPresent(perfil -> {
            if (promedio < PROMEDIO_MINIMO && !perfil.isPausada()) {
                perfil.setPausada(true);
                perfilRepository.save(perfil);
                log.warn("RN-09: cocinera {} pausada por promedio {}", cocineraId, promedio);
            }
        });
    }
}