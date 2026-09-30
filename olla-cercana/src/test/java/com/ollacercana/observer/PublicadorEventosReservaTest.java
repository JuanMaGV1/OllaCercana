package com.ollacercana.observer;

import org.junit.jupiter.api.Test;

import com.ollacercana.model.domain.EventoReserva;
import com.ollacercana.model.domain.TipoEvento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

class PublicadorEventosReservaTest {

    private EventoReserva evento() {
        return new EventoReserva(UUID.randomUUID(), TipoEvento.RESERVA_CONFIRMADA, UUID.randomUUID(),
                UUID.randomUUID(), 1L, UUID.randomUUID(), LocalDateTime.now(), Map.of());
    }

    @Test
    void publicar_debeNotificarATodosLosObservadores() {
        com.ollacercana.observer.ObservadorReserva primero = mock(com.ollacercana.observer.ObservadorReserva.class);
        com.ollacercana.observer.ObservadorReserva segundo = mock(com.ollacercana.observer.ObservadorReserva.class);
        com.ollacercana.observer.PublicadorEventosReserva publicador = new com.ollacercana.observer.PublicadorEventosReserva(List.of(primero, segundo));
        EventoReserva evento = evento();

        publicador.publicar(evento);

        verify(primero).notificar(evento);
        verify(segundo).notificar(evento);
    }

    @Test
    void publicar_siUnObservadorFalla_losDemasIgualReciben() {
        com.ollacercana.observer.ObservadorReserva conFallo = mock(com.ollacercana.observer.ObservadorReserva.class);
        com.ollacercana.observer.ObservadorReserva sano = mock(com.ollacercana.observer.ObservadorReserva.class);
        EventoReserva evento = evento();
        doThrow(new RuntimeException("push caído")).when(conFallo).notificar(evento);
        com.ollacercana.observer.PublicadorEventosReserva publicador = new com.ollacercana.observer.PublicadorEventosReserva(List.of(conFallo, sano));

        publicador.publicar(evento);

        verify(sano).notificar(evento);
    }
}
