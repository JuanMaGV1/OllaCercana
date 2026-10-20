package com.ollacercana.observer;

import com.ollacercana.core.models.EventoReserva;
import com.ollacercana.core.models.Notificacion;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.models.enums.TipoEvento;
import com.ollacercana.core.models.enums.TipoNotificacion;
import com.ollacercana.core.patterns.observer.NotificacionInAppObservador;
import com.ollacercana.persistence.document.NotificacionDocument;
import com.ollacercana.persistence.entities.CuentaEntity;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.mappers.NotificacionDocumentMapper;
import com.ollacercana.persistence.repository.CuentaRepository;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.mongo.NotificacionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
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
    @Mock private CuentaRepository cuentaRepository;
    @Mock private PerfilCocineraRepository perfilCocineraRepository;

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
        verify(notificacionRepository, atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void reservaConfirmada_debeAvisarAlCompradorConLaHoraEstimada() {
        when(cuentaRepository.findById(COMPRADOR_ID))
                .thenReturn(Optional.of(CuentaEntity.builder().id(COMPRADOR_ID).avisosActivos(true).build()));

        EventoReserva evento = evento(TipoEvento.RESERVA_CONFIRMADA,
                Map.of("horaEstimada", LocalDateTime.of(2026, 10, 1, 12, 30)));

        observador.notificar(evento);

        NotificacionDocument notificacion = notificacionGuardada();
        assertEquals(Rol.COMPRADOR, notificacion.getRolDestinatario());
        assertEquals(COMPRADOR_ID, notificacion.getCompradorId());
        assertEquals(TipoNotificacion.RESERVA_CONFIRMADA, notificacion.getTipo());
        assertTrue(notificacion.getMensaje().contains("12:30"));
    }

    @Test
    @DisplayName("OC-294: Cuando avisosActivos es false, se registra en historial y se omite push")
    void reservaConfirmada_conAvisosDesactivados_guardaEnHistorial() {
        when(cuentaRepository.findById(COMPRADOR_ID))
                .thenReturn(Optional.of(CuentaEntity.builder().id(COMPRADOR_ID).avisosActivos(false).build()));

        EventoReserva evento = evento(TipoEvento.RESERVA_CONFIRMADA,
                Map.of("horaEstimada", LocalDateTime.of(2026, 10, 1, 12, 30)));

        observador.notificar(evento);

        verify(notificacionRepository, atLeastOnce()).save(any());
    }

    @Test
    void reservaRechazada_debeAvisarAlCompradorConElMotivo() {
        observador.notificar(evento(TipoEvento.RESERVA_RECHAZADA,
                Map.of("motivo", "Otro motivo", "comentario", "Se me dañó la estufa")));

        NotificacionDocument notificacion = notificacionGuardada();
        assertEquals(TipoNotificacion.RESERVA_RECHAZADA, notificacion.getTipo());
        assertTrue(notificacion.getMensaje().contains("Otro motivo (Se me dañó la estufa)"));
    }

    @Test
    void reservaExpirada_debeAvisarAlComprador() {
        observador.notificar(evento(TipoEvento.RESERVA_EXPIRADA, Map.of()));
        NotificacionDocument notificacion = notificacionGuardada();
        assertEquals(TipoNotificacion.RESERVA_EXPIRADA, notificacion.getTipo());
    }

    @Test
    void recordatorio_debeAvisarALaCocineraYResolverSuCuenta() {
        when(perfilCocineraRepository.findById(COCINERA_ID))
                .thenReturn(Optional.of(PerfilCocineraEntity.builder()
                        .cuenta(CuentaEntity.builder().id(99L).avisosActivos(true).build())
                        .build()));
        when(cuentaRepository.findById(99L))
                .thenReturn(Optional.of(CuentaEntity.builder().id(99L).avisosActivos(true).build()));

        observador.notificar(evento(TipoEvento.RECORDATORIO_RESERVA, Map.of("minutosRestantes", 3L)));

        NotificacionDocument notificacion = notificacionGuardada();
        assertEquals(Rol.COCINERA, notificacion.getRolDestinatario());
        assertEquals(COCINERA_ID, notificacion.getCocineraId());
        assertEquals(TipoNotificacion.RECORDATORIO, notificacion.getTipo());
    }

    @Test
    void recordatorioRecogida_debeAvisarAlComprador() {
        observador.notificar(evento(TipoEvento.RECORDATORIO_RESERVA, Map.of("recordatorioRecogida", true)));
        NotificacionDocument notificacion = notificacionGuardada();
        assertEquals(Rol.COMPRADOR, notificacion.getRolDestinatario());
        assertTrue(notificacion.getMensaje().contains("15 minutos"));
    }

    @Test
    void reservaCompletada_avisaAAmbasPartes() {
        observador.notificar(evento(TipoEvento.RESERVA_COMPLETADA, Map.of("automatica", true)));
        verify(notificacionRepository, times(2)).save(any());
    }

    @Test
    void eventoSinNotificacion_noDebeGuardarNada() {
        observador.notificar(evento(TipoEvento.DISPONIBILIDAD_ACTUALIZADA, null));
        verify(notificacionRepository, never()).save(any());
    }
}