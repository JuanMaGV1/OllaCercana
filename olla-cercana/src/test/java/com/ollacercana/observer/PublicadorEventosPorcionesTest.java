package com.ollacercana.observer;

import com.ollacercana.domain.EstadoPorciones;

import com.ollacercana.dto.response.PorcionesActualizadasResponseDTO;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.Plato;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PublicadorEventosPorcionesTest {

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private PublicadorEventosPorciones publicador;

    private Plato plato(int totales, int comprometidas, EstadoPlato estado) {
        return Plato.builder()
                .id(UUID.randomUUID())
                .porcionesTotales(totales)
                .porcionesComprometidas(comprometidas)
                .estado(estado)
                .build();
    }

    @Test
    @DisplayName("Publica el evento con el stock y el estado correctos cuando el stock cambia")
    void stockCambia_PublicaEvento() {
        Plato plato = plato(3, 1, EstadoPlato.ACTIVO); // quedan 2

        publicador.publicarSiCambio(3, plato);         // antes había 3

        ArgumentCaptor<PorcionesActualizadasResponseDTO> captor =
                ArgumentCaptor.forClass(PorcionesActualizadasResponseDTO.class);
        verify(publisher).publishEvent(captor.capture());
        assertEquals(plato.getId(), captor.getValue().platoId());
        assertEquals(2, captor.getValue().porcionesDisponibles());
        assertEquals(EstadoPorciones.DISPONIBLE, captor.getValue().estado());
    }

    @Test
    @DisplayName("No publica nada si el stock no cambió")
    void stockIgual_NoPublica() {
        Plato plato = plato(3, 1, EstadoPlato.ACTIVO);

        publicador.publicarSiCambio(2, plato);

        verify(publisher, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Publica un evento AGOTADO cuando se reserva la última porción")
    void ultimaPorcion_PublicaAgotado() {
        Plato plato = plato(2, 2, EstadoPlato.AGOTADO);

        publicador.publicarSiCambio(1, plato);

        ArgumentCaptor<PorcionesActualizadasResponseDTO> captor =
                ArgumentCaptor.forClass(PorcionesActualizadasResponseDTO.class);
        verify(publisher).publishEvent(captor.capture());
        assertEquals(0, captor.getValue().porcionesDisponibles());
        assertEquals(EstadoPorciones.AGOTADO, captor.getValue().estado());
    }
}
