package com.ollacercana.config.scheduler;

import com.ollacercana.core.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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