package com.ollacercana.core.patterns.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ollacercana.core.models.EventoReserva;

import java.util.List;

/**
 * Patrón Observer — punto único de publicación de eventos de reserva.
 *
 * Implementado en Sprint 2 para desacoplar el dominio de las acciones
 * reactivas (auditoría, notificaciones in-app, otorgamiento de medallas).
 *
 * FEAT-11 — Interacción (chat y notificaciones) — OC-49
 *
 * @see OC-133 Entidad EventoReserva + enum TipoEvento
 * @see OC-263 Publicación del evento NUEVO_MENSAJE_CHAT
 *
 * @implNote Si un observador falla, los demás siguen ejecutándose — se loguea
 *           el error pero no se propaga para no romper el flujo principal.
 */

@Component
public class PublicadorEventosReserva {

    private static final Logger log = LoggerFactory.getLogger(PublicadorEventosReserva.class);

    private final List<com.ollacercana.core.patterns.observer.ObservadorReserva> observadores;

    public PublicadorEventosReserva(List<com.ollacercana.core.patterns.observer.ObservadorReserva> observadores) {
        this.observadores = List.copyOf(observadores);
    }

    public void publicar(EventoReserva evento) {
        for (com.ollacercana.core.patterns.observer.ObservadorReserva observador : observadores) {
            try {
                observador.notificar(evento);
            } catch (RuntimeException e) {
                log.error("El observador {} falló procesando el evento {} de la reserva {}: {}",
                        observador.getClass().getSimpleName(), evento.tipo(), evento.reservaId(), e.getMessage());
            }
        }
    }
}
