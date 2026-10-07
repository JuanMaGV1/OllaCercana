package com.ollacercana.observer;

import com.ollacercana.controller.dtos.response.PorcionesActualizadasResponseDTO;
import com.ollacercana.core.models.enums.EstadoPorciones;
import com.ollacercana.core.patterns.observer.NotificadorPorcionesImpl;
import com.ollacercana.core.patterns.observer.ObservadorPorciones;
import com.ollacercana.core.patterns.observer.RegistroSuscripciones;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificadorPorcionesImplTest {

    private final RegistroSuscripciones registro = new RegistroSuscripciones();
    private final NotificadorPorcionesImpl notificador = new NotificadorPorcionesImpl(registro);

    private final UUID platoA = UUID.randomUUID();
    private final UUID platoB = UUID.randomUUID();

    private static class SuscriptorFalso implements ObservadorPorciones {
        final List<PorcionesActualizadasResponseDTO> recibidos = new ArrayList<>();

        @Override
        public void enviar(PorcionesActualizadasResponseDTO evento) {
            recibidos.add(evento);
        }
    }

    @Test
    @DisplayName("Un suscriptor del plato A recibe los eventos del plato A")
    void suscriptorRecibeEventoDeSuPlato() {
        var suscriptor = new SuscriptorFalso();
        registro.suscribir(platoA, suscriptor);
        var evento = new PorcionesActualizadasResponseDTO(platoA, 1, EstadoPorciones.DISPONIBLE);

        notificador.notificar(evento);

        assertEquals(List.of(evento), suscriptor.recibidos);
    }

    @Test
    @DisplayName("Un suscriptor del plato A no recibe eventos del plato B")
    void suscriptorNoRecibeEventosDeOtroPlato() {
        var suscriptor = new SuscriptorFalso();
        registro.suscribir(platoA, suscriptor);

        notificador.notificar(new PorcionesActualizadasResponseDTO(platoB, 0, EstadoPorciones.AGOTADO));

        assertTrue(suscriptor.recibidos.isEmpty());
    }

    @Test
    @DisplayName("Un suscriptor caído se elimina y los demás siguen recibiendo")
    void suscriptorCaido_SeEliminaYLosDemasContinuan() {
        ObservadorPorciones caido = evento -> { throw new IOException("conexión cerrada"); };
        var sano = new SuscriptorFalso();
        registro.suscribir(platoA, caido);
        registro.suscribir(platoA, sano);
        var evento = new PorcionesActualizadasResponseDTO(platoA, 1, EstadoPorciones.DISPONIBLE);

        notificador.notificar(evento);

        assertEquals(List.of(evento), sano.recibidos);
        assertEquals(1, registro.total());
        assertTrue(registro.de(platoA).contains(sano));
    }

    @Test
    @DisplayName("Notificar sin suscriptores no falla")
    void sinSuscriptores_NoFalla() {
        notificador.notificar(new PorcionesActualizadasResponseDTO(platoA, 2, EstadoPorciones.DISPONIBLE));
    }
}
