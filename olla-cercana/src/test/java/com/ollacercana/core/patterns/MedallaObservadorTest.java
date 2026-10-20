package com.ollacercana.core.patterns;

import com.ollacercana.core.models.EventoReserva;
import com.ollacercana.core.models.enums.TipoEvento;
import com.ollacercana.core.patterns.observer.MedallaObservador;
import com.ollacercana.core.services.MedallaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedallaObservadorTest {

    @Mock
    private MedallaService medallaService;

    @Test
    @DisplayName("Al completarse una reserva evalúa la insignia Vecino Fiel")
    void evaluaAlCompletar() {
        UUID cocineraId = UUID.randomUUID();
        new MedallaObservador(medallaService).notificar(evento(TipoEvento.RESERVA_COMPLETADA, 7L, cocineraId));

        verify(medallaService).evaluarVecinoFiel(eq(7L), eq(cocineraId), any());
    }

    @Test
    @DisplayName("Otros eventos de reserva no evalúan la insignia")
    void ignoraOtrosEventos() {
        new MedallaObservador(medallaService).notificar(evento(TipoEvento.RESERVA_CONFIRMADA, 7L, UUID.randomUUID()));

        verify(medallaService, never()).evaluarVecinoFiel(any(), any(), any());
    }

    @Test
    @DisplayName("Si la evaluación falla, el observador no propaga el error")
    void noPropagaErrores() {
        when(medallaService.evaluarVecinoFiel(any(), any(), any())).thenThrow(new IllegalStateException("fallo"));

        assertDoesNotThrow(() -> new MedallaObservador(medallaService)
                .notificar(evento(TipoEvento.RESERVA_COMPLETADA, 7L, UUID.randomUUID())));
    }

    private EventoReserva evento(TipoEvento tipo, Long compradorId, UUID cocineraId) {
        return EventoReserva.builder()
                .id(UUID.randomUUID().toString())
                .tipo(tipo)
                .reservaId(UUID.randomUUID())
                .compradorId(compradorId)
                .cocineraId(cocineraId)
                .build();
    }
}