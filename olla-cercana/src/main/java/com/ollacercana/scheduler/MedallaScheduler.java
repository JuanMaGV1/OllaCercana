package com.ollacercana.scheduler;

import com.ollacercana.service.MedallaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * HU-21 / OC-299: tarea semanal del balance de conjuntos (por defecto, lunes a las 00:00).
 */
@Component
@RequiredArgsConstructor
public class MedallaScheduler {

    private static final Logger log = LoggerFactory.getLogger(MedallaScheduler.class);

    private final MedallaService medallaService;

    @Scheduled(cron = "${ollacercana.medallas.cron-balance:0 0 0 * * MON}")
    public void calcularBalanceSemanal() {
        try {
            medallaService.calcularBalanceSemanal(LocalDateTime.now());
        } catch (RuntimeException e) {
            log.error("Falló el balance semanal de conjuntos: {}", e.getMessage());
        }
    }
}
