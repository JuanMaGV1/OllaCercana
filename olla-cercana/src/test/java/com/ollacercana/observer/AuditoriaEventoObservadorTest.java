package com.ollacercana.observer;

import com.ollacercana.core.models.EventoReserva;
import com.ollacercana.core.models.enums.TipoEvento;
import com.ollacercana.core.patterns.observer.AuditoriaEventoObservador;
import com.ollacercana.persistence.document.EventoReservaDocument;
import com.ollacercana.persistence.mappers.EventoMapper;
import com.ollacercana.persistence.repository.mongo.EventoReservaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaEventoObservadorTest {

    @Mock private EventoReservaRepository eventoReservaRepository;
    @Mock private EventoMapper eventoMapper;

    @InjectMocks private AuditoriaEventoObservador observador;

    @Test
    @DisplayName("notificar: guarda el evento en el repositorio de auditoría")
    void notificar_guardaDocumento() {
        EventoReserva evento = EventoReserva.builder()
                .id(UUID.randomUUID().toString())
                .tipo(TipoEvento.RESERVA_CREADA)
                .reservaId(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .build();

        EventoReservaDocument doc = new EventoReservaDocument();
        when(eventoMapper.toDocument(evento)).thenReturn(doc);

        observador.notificar(evento);

        verify(eventoMapper).toDocument(evento);
        verify(eventoReservaRepository).save(doc);
    }
}