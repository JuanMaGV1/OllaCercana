package com.ollacercana.config.scheduler;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ollacercana.core.services.MedallaService;

import java.time.LocalDateTime;

/**
 * Tarea programada semanal que calcula el balance de conjuntos residenciales
 * y otorga la medalla "Conjunto Olla Verde" a los que vendieron el 100% de sus porciones.
 *
 * <p>HU-21 — Retos comunitarios e insignias de fidelidad
 * <p>OC-299 — Calcular balance semanal y asignar "Conjunto Olla Verde"
 * <p>OC-300 / OC-301 — Maquetas front (fuera de alcance backend)
 *
 * <p>Se ejecuta por defecto los lunes a las 00:00.
 * <p>Expresión cron configurable: {@code ollacercana.medallas.cron-balance}.
 *
 * <p>Vigencia de la medalla: 7 días desde el otorgamiento.
 *
 * @see com.ollacercana.core.services.MedallaService#calcularBalanceSemanal(java.time.LocalDateTime)
 * @see com.ollacercana.core.services.impl.MedallaServiceImpl
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
