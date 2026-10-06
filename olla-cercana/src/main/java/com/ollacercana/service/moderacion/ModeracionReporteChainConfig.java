package com.ollacercana.service.moderacion;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
