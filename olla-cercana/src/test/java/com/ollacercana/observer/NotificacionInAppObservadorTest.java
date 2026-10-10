package com.ollacercana.observer;

import com.ollacercana.core.models.EventoReserva;
import com.ollacercana.core.models.Notificacion;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.models.enums.TipoEvento;
import com.ollacercana.core.models.enums.TipoNotificacion;
import com.ollacercana.core.patterns.observer.NotificacionInAppObservador;
import com.ollacercana.persistence.document.NotificacionDocument;
import com.ollacercana.persistence.mappers.NotificacionDocumentMapper;
import com.ollacercana.persistence.repository.mongo.NotificacionRepository;

import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionInAppObservadorTest {

    private static final Long COMPRADOR_ID = 42L;
    private static final UUID COCINERA_ID = UUID.randomUUID();

    @Mock private NotificacionRepository notificacionRepository;
    @Mock private NotificacionDocumentMapper notificacionMapper;

    @InjectMocks private NotificacionInAppObservador observador;

    @BeforeEach
    void setUp() {
        lenient().when(notificacionMapper.toDocument(any(Notificacion.class))).thenAnswer(i -> {
            Notificacion n = i.getArgument(0);
            return NotificacionDocument.builder()
                    .reservaId(n.getReservaId())
                    .rolDestinatario(n.getRolDestinatario())
                    .compradorId(n.getCompradorId())
                    .cocineraId(n.getCocineraId())
                    .titulo(n.getTitulo())
                    .mensaje(n.getMensaje())
                    .tipo(n.getTipo())
                    .leida(n.isLeida())
                    .fechaCreacion(n.getFechaCreacion())
                    .build();
        });
    }

    private EventoReserva evento(TipoEvento tipo, Map<String, Object> payload) {
        return new EventoReserva(
                UUID.randomUUID().toString(),
                tipo,
                UUID.randomUUID(),
                UUID.randomUUID(),
                COMPRADOR_ID,
                COCINERA_ID,
                LocalDateTime.now(),
                payload);
    }

    private NotificacionDocument notificacionGuardada() {
        ArgumentCaptor<NotificacionDocument> captor = ArgumentCaptor.forClass(NotificacionDocument.class);
        verify(notificacionRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void reservaConfirmada_debeAvisarAlCompradorConLaHoraEstimada() {
        EventoReserva evento = evento(TipoEvento.RESERVA_CONFIRMADA,
                Map.of("horaEstimada", LocalDateTime.of(2026, 10, 1, 12, 30)));

        observador.notificar(evento);

        NotificacionDocument notificacion = notificacionGuardada();
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

        NotificacionDocument notificacion = notificacionGuardada();
        assertEquals(TipoNotificacion.RESERVA_RECHAZADA, notificacion.getTipo());
        assertEquals(COMPRADOR_ID, notificacion.getCompradorId());
        assertTrue(notificacion.getMensaje().contains("Otro motivo (Se me dañó la estufa)"));
    }

    @Test
    void reservaExpirada_debeAvisarAlComprador() {
        observador.notificar(evento(TipoEvento.RESERVA_EXPIRADA, Map.of()));

        NotificacionDocument notificacion = notificacionGuardada();
        assertEquals(TipoNotificacion.RESERVA_EXPIRADA, notificacion.getTipo());
        assertEquals(Rol.COMPRADOR, notificacion.getRolDestinatario());
    }

    @Test
    void recordatorio_debeAvisarALaCocinera() {
        observador.notificar(evento(TipoEvento.RECORDATORIO_RESERVA,
                Map.of("minutosRestantes", 3L)));

        NotificacionDocument notificacion = notificacionGuardada();
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