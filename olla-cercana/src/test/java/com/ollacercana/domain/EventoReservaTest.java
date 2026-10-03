package com.ollacercana.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventoReservaTest {

    @Test
    @DisplayName("Constructores y métodos accesores manejan nulos correctamente")
    void constructoresYMetodos() {
        EventoReserva evento = new EventoReserva(null, TipoEvento.RESERVA_CREADA, UUID.randomUUID(),
                UUID.randomUUID(), 1L, UUID.randomUUID(), null, null);

        assertNotNull(evento.getId());
        assertNotNull(evento.timestamp());
        assertNotNull(evento.payload());
        assertTrue(evento.payload().isEmpty());

        EventoReserva creado = EventoReserva.de(TipoEvento.RESERVA_CONFIRMADA,
                Reserva.builder().id(UUID.randomUUID()).platoId(UUID.randomUUID()).compradorId(2L).cocineraId(UUID.randomUUID()).build(),
                null);

        assertNotNull(creado.getPayload());
        assertEquals(TipoEvento.RESERVA_CONFIRMADA, creado.tipo());
    }
}