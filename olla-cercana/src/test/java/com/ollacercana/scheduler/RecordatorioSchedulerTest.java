package com.ollacercana.scheduler;

import com.ollacercana.config.scheduler.RecordatorioScheduler;
import com.ollacercana.core.models.EventoReserva;
import com.ollacercana.core.models.Reserva;
import com.ollacercana.core.patterns.observer.PublicadorEventosReserva;
import com.ollacercana.core.services.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordatorioSchedulerTest {

    @Mock private ReservaService reservaService;
    @Mock private PublicadorEventosReserva publicador;

    @InjectMocks private RecordatorioScheduler scheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "tareasProgramadas", true);
    }

    @Test
    @DisplayName("HU-17: publica el evento y marca la reserva cuando hay recordatorios")
    void recordatorioRecogida_publica() {
        UUID id = UUID.randomUUID();
        Reserva reserva = Reserva.builder()
                .id(id)
                .platoId(UUID.randomUUID())
                .compradorId(42L)
                .cocineraId(UUID.randomUUID())
                .build();

        when(reservaService.buscarReservasParaRecordatorioRecogida(any()))
                .thenReturn(List.of(id));
        when(reservaService.obtenerPorId(id)).thenReturn(reserva);

        scheduler.recordatorioRecogida();

        ArgumentCaptor<EventoReserva> captor = ArgumentCaptor.forClass(EventoReserva.class);
        verify(publicador).publicar(captor.capture());
        assertEquals(id, captor.getValue().reservaId());
        assertEquals(true, captor.getValue().payload().get("recordatorioRecogida"));
        verify(reservaService).marcarRecordatorioRecogidaEnviado(id);
    }

    @Test
    @DisplayName("HU-17: con flag deshabilitado no hace nada")
    void recordatorioRecogida_flagDeshabilitado() {
        ReflectionTestUtils.setField(scheduler, "tareasProgramadas", false);
        scheduler.recordatorioRecogida();
        verifyNoInteractions(reservaService, publicador);
    }

    @Test
    @DisplayName("HU-17: sin reservas no hace nada")
    void recordatorioRecogida_sinReservas() {
        when(reservaService.buscarReservasParaRecordatorioRecogida(any()))
                .thenReturn(List.of());

        scheduler.recordatorioRecogida();

        verifyNoInteractions(publicador);
    }
}