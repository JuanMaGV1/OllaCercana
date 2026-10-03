package com.ollacercana.observer;

import com.ollacercana.domain.EventoReserva;
import com.ollacercana.domain.Notificacion;
import com.ollacercana.domain.Rol;
import com.ollacercana.domain.TipoEvento;
import com.ollacercana.domain.TipoNotificacion;
import com.ollacercana.repository.mongo.NotificacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * HU-12 Escenario 3 (aviso al comprador) y OC-149 (recordatorio a la cocinera).
 */
@ExtendWith(MockitoExtension.class)
class NotificacionInAppObservadorTest {

    private static final Long COMPRADOR_ID = 42L;
    private static final UUID COCINERA_ID = UUID.randomUUID();

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private com.ollacercana.observer.NotificacionInAppObservador observador;

    private EventoReserva evento(TipoEvento tipo, Map<String, Object> payload) {
        return new EventoReserva(UUID.randomUUID(), tipo, UUID.randomUUID(), UUID.randomUUID(),
                COMPRADOR_ID, COCINERA_ID, LocalDateTime.now(), payload);
    }

    private Notificacion notificacionGuardada() {
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void reservaConfirmada_debeAvisarAlCompradorConLaHoraEstimada() {
        EventoReserva evento = evento(TipoEvento.RESERVA_CONFIRMADA,
                Map.of("horaEstimada", LocalDateTime.of(2026, 10, 1, 12, 30)));

        observador.notificar(evento);

        Notificacion notificacion = notificacionGuardada();
        assertEquals(Rol.COMPRADOR, notificacion.getRolDestinatario());
        assertEquals(COMPRADOR_ID, notificacion.getCompradorId());
        assertEquals(TipoNotificacion.RESERVA_CONFIRMADA, notificacion.getTipo());
        assertEquals(evento.reservaId(), notificacion.getReservaId());
        assertTrue(notificacion.getMensaje().contains("12:30"));
        assertFalse(notificacion.isLeida());
    }

    @Test
    void reservaRechazada_debeAvisarAlCompradorConElMotivo() {
        observador.notificar(evento(TipoEvento.RESERVA_RECHAZADA,
                Map.of("motivo", "Otro motivo", "comentario", "Se me dañó la estufa")));

        Notificacion notificacion = notificacionGuardada();
        assertEquals(TipoNotificacion.RESERVA_RECHAZADA, notificacion.getTipo());
        assertEquals(COMPRADOR_ID, notificacion.getCompradorId());
        assertTrue(notificacion.getMensaje().contains("Otro motivo (Se me dañó la estufa)"));
    }

    @Test
    void reservaExpirada_debeAvisarAlComprador() {
        observador.notificar(evento(TipoEvento.RESERVA_EXPIRADA, Map.of()));

        Notificacion notificacion = notificacionGuardada();
        assertEquals(TipoNotificacion.RESERVA_EXPIRADA, notificacion.getTipo());
        assertEquals(Rol.COMPRADOR, notificacion.getRolDestinatario());
    }

    @Test
    void recordatorio_debeAvisarALaCocinera() {
        observador.notificar(evento(TipoEvento.RECORDATORIO_RESERVA, Map.of("minutosRestantes", 3L)));

        Notificacion notificacion = notificacionGuardada();
        assertEquals(Rol.COCINERA, notificacion.getRolDestinatario());
        assertEquals(COCINERA_ID, notificacion.getCocineraId());
        assertNull(notificacion.getCompradorId());
        assertEquals(TipoNotificacion.RECORDATORIO, notificacion.getTipo());
        assertTrue(notificacion.getMensaje().contains("3 minutos"));
    }

    @Test
    void eventoSinNotificacion_noDebeGuardarNada() {
        observador.notificar(evento(TipoEvento.DISPONIBILIDAD_ACTUALIZADA, null));

        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void marcarLeida_debeCambiarElEstado() {
        Notificacion notificacion = Notificacion.builder().leida(false).build();

        notificacion.marcarLeida();

        assertTrue(notificacion.isLeida());
    }
}
