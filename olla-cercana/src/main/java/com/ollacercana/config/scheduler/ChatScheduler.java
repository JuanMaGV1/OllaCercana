package com.ollacercana.config.scheduler;

import com.ollacercana.core.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tarea programada que purga los mensajes de chat de reservas cerradas hace más de 30 días.
 *
 * HU-13 — Coordinación de entrega por chat
 * RN-18 — Retención de historial de chat durante 30 días
 * OC-266 — Programar la purga de mensajes a los 30 días
 *
 * Respeta el flag {@code ollacercana.reservas.tareas-programadas}.
 * Intervalo configurable: {@code ollacercana.chat.intervalo-purga-ms}
 * (por defecto 86400000 ms = 24 h).
 *
 * @see com.ollacercana.core.services.ChatService#purgarMensajesAntiguos()
 * @see com.ollacercana.core.services.impl.ChatServiceImpl
 */
@Component
@RequiredArgsConstructor
public class ChatScheduler {

    private static final Logger log = LoggerFactory.getLogger(ChatScheduler.class);

    private final ChatService chatService;

    @Value("${ollacercana.reservas.tareas-programadas:true}")
    private boolean tareasProgramadasHabilitadas;

    @Scheduled(fixedRateString = "${ollacercana.chat.intervalo-purga-ms:86400000}",
            initialDelayString = "${ollacercana.chat.intervalo-purga-ms:60000}")
    public void purgarMensajesAntiguos() {
        if (!tareasProgramadasHabilitadas) {
            return;
        }
        try {
            long eliminados = chatService.purgarMensajesAntiguos();
            log.info("Purga de chat ejecutada (RN-18): {} mensajes eliminados de reservas cerradas", eliminados);
        } catch (Exception e) {
            log.warn("No se pudo ejecutar la purga de mensajes de chat: {}", e.getMessage());
        }
    }
}