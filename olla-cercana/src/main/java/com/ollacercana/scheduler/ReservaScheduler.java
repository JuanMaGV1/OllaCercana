package com.ollacercana.scheduler;

import com.ollacercana.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

   
                                                                                        
                                                                            
   
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
