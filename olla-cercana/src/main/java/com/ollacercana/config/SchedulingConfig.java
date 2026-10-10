package com.ollacercana.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita el motor de tareas programadas de Spring.
 *
 * Se activa por defecto, pero se puede deshabilitar con:
 * 
 * ollacercana.reservas.tareas-programadas=false
 * 
 * Útil en tests unitarios o en escenarios donde se quieren controlar
 * manualmente los tiempos.
 *
 * @see CalificacionScheduler
 * @see ChatScheduler
 * @see MedallaScheduler
 * @see RecordatorioScheduler
 * @see ReservaScheduler
 */                                                                                    
   
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "ollacercana.reservas.tareas-programadas", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
