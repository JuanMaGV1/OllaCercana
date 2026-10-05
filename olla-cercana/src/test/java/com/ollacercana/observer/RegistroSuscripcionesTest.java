package com.ollacercana.observer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroSuscripcionesTest {

    private final RegistroSuscripciones registro = new RegistroSuscripciones();
    private final UUID platoA = UUID.randomUUID();
    private final UUID platoB = UUID.randomUUID();

    private final ObservadorPorciones s1 = evento -> { };
    private final ObservadorPorciones s2 = evento -> { };

    @Test
    @DisplayName("Los suscriptores quedan agrupados por plato")
    void suscribir_AgrupaPorPlato() {
        registro.suscribir(platoA, s1);
        registro.suscribir(platoB, s2);

        assertEquals(1, registro.de(platoA).size());
        assertTrue(registro.de(platoA).contains(s1));
        assertEquals(1, registro.de(platoB).size());
        assertEquals(2, registro.total());
    }

    @Test
    @DisplayName("Cancelar elimina al suscriptor y deja el plato sin entradas vacías")
    void cancelar_EliminaSuscriptor() {
        registro.suscribir(platoA, s1);

        registro.cancelar(platoA, s1);

        assertTrue(registro.de(platoA).isEmpty());
        assertEquals(0, registro.total());
    }

    @Test
    @DisplayName("Consultar un plato sin suscriptores devuelve una lista vacía")
    void platoSinSuscriptores_ListaVacia() {
        assertTrue(registro.de(UUID.randomUUID()).isEmpty());
    }
}
