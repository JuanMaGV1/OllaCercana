package com.ollacercana.config.scheduler;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ollacercana.core.services.ReservaService;

import java.util.UUID;
                                                                        
/**
 * Tarea programada con tres responsabilidades sobre el ciclo de vida de reservas.
 *
 * HU-12 — Aceptar o rechazar solicitudes
 * HU-23 — Confirmar entrega y cerrar transacción
 * RN-04 — Expiración a los 10 minutos si la cocinera no responde
 * RN-25 — Recordatorio a la cocinera a los 7 min
 * RN-33 — Cierre automático a las 24 h si nadie confirma
 *
 * Ejecuta cada minuto (configurable):
 *   {@code expirarReservasVencidas()} — RN-04 · OC-148
 *   {@code completarReservasSinCierre()} — RN-33 · OC-157
 *   {@code enviarRecordatorios()} — RN-25 · OC-149
 *
 * @see com.ollacercana.core.services.ReservaService
 * @see com.ollacercana.core.services.impl.ReservaServiceImpl
 */                                                                            
   
@Component
@RequiredArgsConstructor
public class ReservaScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservaScheduler.class);
    private final ReservaService reservaService;                               
       
    @Scheduled(fixedRateString = "${ollacercana.reservas.intervalo-revision-ms:60000}",
            initialDelayString = "${ollacercana.reservas.intervalo-revision-ms:60000}")
    public void expirarReservasVencidas() {
        for (UUID reservaId : reservaService.buscarReservasVencidas()) {
            try {
                reservaService.expirar(reservaId);
                log.info("Reserva {} expirada por falta de respuesta (RN-04)", reservaId);
            } catch (RuntimeException e) {
                log.warn("No se pudo expirar la reserva {}: {}", reservaId, e.getMessage());
            }
        }
    }                                                                                                      
       
    @Scheduled(fixedRateString = "${ollacercana.reservas.intervalo-cierre-ms:600000}",
            initialDelayString = "${ollacercana.reservas.intervalo-cierre-ms:600000}")
    public void completarReservasSinCierre() {
        for (UUID reservaId : reservaService.buscarReservasParaCierreAutomatico()) {
            try {
                reservaService.completarAutomaticamente(reservaId);
                log.info("Reserva {} revisada para cierre automático a las 24 h (HU-23)", reservaId);
            } catch (RuntimeException e) {
                log.warn("No se pudo completar automáticamente la reserva {}: {}", reservaId, e.getMessage());
            }
        }
    }                                                                                               
       
    @Scheduled(fixedRateString = "${ollacercana.reservas.intervalo-revision-ms:60000}",
            initialDelayString = "${ollacercana.reservas.intervalo-revision-ms:60000}")
    public void enviarRecordatorios() {
        for (UUID reservaId : reservaService.buscarReservasParaRecordatorio()) {
            try {
                reservaService.enviarRecordatorio(reservaId);
                log.info("Recordatorio enviado a la cocinera por la reserva {} (RN-25)", reservaId);
            } catch (RuntimeException e) {
                log.warn("No se pudo enviar el recordatorio de la reserva {}: {}", reservaId, e.getMessage());
            }
        }
    }
}
