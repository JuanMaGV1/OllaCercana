package com.ollacercana.config.scheduler;

import com.ollacercana.core.models.EventoReserva;
import com.ollacercana.core.models.Reserva;
import com.ollacercana.core.models.enums.TipoEvento;
import com.ollacercana.core.patterns.observer.PublicadorEventosReserva;
import com.ollacercana.core.services.ReservaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Tarea programada que envía recordatorios de recogida 15 minutos antes de la hora acordada.
 *
 * HU-17 — Recepción de avisos del estado de la reserva y mensajes
 * RN-25 — Recordatorio al comprador 15 min antes de la hora de entrega
 * OC-291 — Maqueta alerta (front)
 * OC-292 — Programar el recordatorio 15 min antes
 *
 * Publica un evento {@code RECORDATORIO_RESERVA} con flag
 * {@code recordatorioRecogida=true} para que el {@link com.ollacercana.core.patterns.observer.NotificacionInAppObservador}
 * lo persista.
 *
 * Respeta el flag {@code ollacercana.notificaciones.tareas-programadas}.
 * Intervalo configurable: {@code ollacercana.notificaciones.intervalo-recordatorio-ms}
 * (por defecto 60000 ms = 1 min).
 *
 * @see com.ollacercana.core.services.ReservaService#buscarReservasParaRecordatorioRecogida(java.time.LocalDateTime)
 * @see com.ollacercana.core.patterns.observer.PublicadorEventosReserva
 */

@Component
@RequiredArgsConstructor
public class RecordatorioScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecordatorioScheduler.class);

    private final ReservaService reservaService;
    private final PublicadorEventosReserva publicador;

    @Value("${ollacercana.notificaciones.tareas-programadas:true}")
    private boolean tareasProgramadas;

    @Scheduled(fixedRateString = "${ollacercana.notificaciones.intervalo-recordatorio-ms:60000}",
            initialDelayString = "${ollacercana.notificaciones.intervalo-recordatorio-ms:60000}")
    public void recordatorioRecogida() {
        if (!tareasProgramadas) return;

        LocalDateTime ahora = LocalDateTime.now();
        for (UUID id : reservaService.buscarReservasParaRecordatorioRecogida(ahora)) {
            try {
                Reserva r = reservaService.obtenerPorId(id);

                EventoReserva evento = EventoReserva.builder()
                        .id(UUID.randomUUID().toString())
                        .tipo(TipoEvento.RECORDATORIO_RESERVA)
                        .reservaId(r.getId())
                        .platoId(r.getPlatoId())
                        .compradorId(r.getCompradorId())
                        .cocineraId(r.getCocineraId())
                        .timestamp(ahora)
                        .payload(Map.of(
                                "recordatorioRecogida", true,
                                "minutosRestantes", 15
                        ))
                        .build();

                publicador.publicar(evento);
                reservaService.marcarRecordatorioRecogidaEnviado(id);

                log.info("HU-17: recordatorio de recogida enviado a la reserva {}", id);
            } catch (RuntimeException e) {
                log.warn("No se pudo enviar recordatorio de recogida para {}: {}", id, e.getMessage());
            }
        }
    }
}