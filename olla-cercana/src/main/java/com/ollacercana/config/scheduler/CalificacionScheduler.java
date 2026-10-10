package com.ollacercana.config.scheduler;

import com.ollacercana.core.services.CalificacionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Tarea programada que publica las calificaciones PENDIENTES cuya ventana de 72 h venció.
 *
 * HU-15 — Calificación del intercambio comunitario
 * RN-19 — Ventana de publicación de 72 h
 * RN-20 — Publicación automática cuando la contraparte no califica
 * OC-196 — Publicación por ventana de 72 h
 *
 * Respeta el flag {@code ollacercana.calificaciones.tareas-programadas}:
 * si está en {@code false} (entorno de pruebas), no ejecuta nada.
 *
 * Intervalo configurable: {@code ollacercana.calificaciones.intervalo-ms}
 * (por defecto 600000 ms = 10 min).
 *
 * @see com.ollacercana.core.services.CalificacionService#publicarPendientesVencidas(java.time.LocalDateTime)
 * @see com.ollacercana.core.services.impl.CalificacionServiceImpl
 */
@Component
@RequiredArgsConstructor
public class CalificacionScheduler {

    private static final Logger log = LoggerFactory.getLogger(CalificacionScheduler.class);

    private final CalificacionService calificacionService;

    @Value("${ollacercana.calificaciones.tareas-programadas:true}")
    private boolean tareasProgramadas;

    @Scheduled(fixedRateString = "${ollacercana.calificaciones.intervalo-ms:600000}",
            initialDelayString = "${ollacercana.calificaciones.intervalo-ms:600000}")
    public void publicarPendientes() {
        if (!tareasProgramadas) return;
        try {
            int publicadas = calificacionService.publicarPendientesVencidas(LocalDateTime.now());
            if (publicadas > 0) log.info("Publicadas {} calificaciones por ventana", publicadas);
        } catch (RuntimeException e) {
            log.warn("No se pudo ejecutar la publicación de calificaciones: {}", e.getMessage());
        }
    }
}