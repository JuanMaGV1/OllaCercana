package com.ollacercana.scheduler;

import com.ollacercana.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * OC-148 / OC-149: las tareas programadas procesan cada reserva por separado.
 */
@ExtendWith(MockitoExtension.class)
class ReservaSchedulerTest {

    @Mock
    private ReservaService reservaService;

    @InjectMocks
    private com.ollacercana.scheduler.ReservaScheduler scheduler;

    @Test
    void expirarReservasVencidas_debeExpirarCadaUnaAunqueUnaFalle() {
        UUID primera = UUID.randomUUID();
        UUID segunda = UUID.randomUUID();
        when(reservaService.buscarReservasVencidas()).thenReturn(List.of(primera, segunda));
        when(reservaService.expirar(primera)).thenThrow(new IllegalStateException("fallo simulado"));

        scheduler.expirarReservasVencidas();

        verify(reservaService).expirar(primera);
        verify(reservaService).expirar(segunda);
    }

    @Test
    void enviarRecordatorios_debeEnviarCadaUnoAunqueUnoFalle() {
        UUID primera = UUID.randomUUID();
        UUID segunda = UUID.randomUUID();
        when(reservaService.buscarReservasParaRecordatorio()).thenReturn(List.of(primera, segunda));
        doThrow(new IllegalStateException("fallo simulado")).when(reservaService).enviarRecordatorio(primera);

        scheduler.enviarRecordatorios();

        verify(reservaService).enviarRecordatorio(primera);
        verify(reservaService).enviarRecordatorio(segunda);
    }

    @Test
    void sinReservasPendientes_noDebeHacerNada() {
        when(reservaService.buscarReservasVencidas()).thenReturn(List.of());
        when(reservaService.buscarReservasParaRecordatorio()).thenReturn(List.of());

        scheduler.expirarReservasVencidas();
        scheduler.enviarRecordatorios();

        verify(reservaService, never()).expirar(any());
        verify(reservaService, never()).enviarRecordatorio(any());
    }
}
