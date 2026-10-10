package com.ollacercana.core.patterns.moderacion;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Patrón Chain of Responsibility — cadena de moderación de reportes.
 *
 * Orden de la cadena:
 *   {@link ValidadorReporteHandler}      — valida formato y reglas básicas
 *   {@link OcultamientoPreventivoHandler} — RN-22: 3 reportantes → OCULTO
 *   {@link EvaluadorReputacionHandler}    — RN-09: impacta reputación
 *   {@link RevisorAdministradorHandler}   — cola de revisión manual
 *
 * @see OC-218 Crear la cadena de moderación
 * @see OC-220 Ocultamiento preventivo con 3 reportantes
 * @see OC-229 ModeracionService.resolver() que dispara la cadena
 */

@Component
@RequiredArgsConstructor
public class ModeracionReporteChainConfig {

    private final ValidadorReporteHandler validadorReporteHandler;
    private final OcultamientoPreventivoHandler ocultamientoPreventivoHandler;
    private final EvaluadorReputacionHandler evaluadorReputacionHandler;
    private final RevisorAdministradorHandler revisorAdministradorHandler;

    @PostConstruct
    public void init() {
        validadorReporteHandler.setNext(ocultamientoPreventivoHandler);
        ocultamientoPreventivoHandler.setNext(evaluadorReputacionHandler);
        evaluadorReputacionHandler.setNext(revisorAdministradorHandler);
    }

    public ModeracionReporteHandler getChain() {
        return validadorReporteHandler;
    }
}