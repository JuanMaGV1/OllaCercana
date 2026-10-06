package com.ollacercana.observer;

import com.ollacercana.model.domain.EventoReserva;
import com.ollacercana.model.domain.TipoEvento;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

class PublicadorEventosReservaTest {

    private EventoReserva evento() {
        // ✅ el id de EventoReserva es String, no UUID
        return new EventoReserva(
                UUID.randomUUID().toString(),
                TipoEvento.RESERVA_CONFIRMADA,
                UUID.randomUUID(),
                UUID.randomUUID(),
                1L,
                UUID.randomUUID(),
                LocalDateTime.now(),
                Map.of());
    }

    @Test
    void publicar_debeNotificarATodosLosObservadores() {
        ObservadorReserva primero = mock(ObservadorReserva.class);
        ObservadorReserva segundo = mock(ObservadorReserva.class);
        PublicadorEventosReserva publicador = new PublicadorEventosReserva(List.of(primero, segundo));
        EventoReserva evento = evento();

        publicador.publicar(evento);

        verify(primero).notificar(evento);
        verify(segundo).notificar(evento);
    }

    @Test
    void publicar_siUnObservadorFalla_losDemasIgualReciben() {
        ObservadorReserva conFallo = mock(ObservadorReserva.class);
        ObservadorReserva sano = mock(ObservadorReserva.class);
        EventoReserva evento = evento();
        doThrow(new RuntimeException("push caído")).when(conFallo).notificar(evento);
        PublicadorEventosReserva publicador = new PublicadorEventosReserva(List.of(conFallo, sano));

        publicador.publicar(evento);

        verify(sano).notificar(evento);
    }
}