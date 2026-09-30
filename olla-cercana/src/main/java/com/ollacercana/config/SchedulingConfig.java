package com.ollacercana.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Activa las tareas @Scheduled (expiración RN-04 y recordatorio RN-25).
 * Se puede apagar con ollacercana.reservas.tareas-programadas=false (útil en pruebas).
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "ollacercana.reservas.tareas-programadas", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
