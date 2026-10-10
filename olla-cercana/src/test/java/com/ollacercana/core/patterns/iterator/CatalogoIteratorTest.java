package com.ollacercana.core.patterns.iterator;

import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.TipoComida;
import com.ollacercana.core.patterns.filter.FiltroTipoComida;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoIteratorTest {

    private Plato plato(TipoComida tipo) {
        return Plato.builder().id(UUID.randomUUID()).tipoComida(tipo).build();
    }

    @Test
    @DisplayName("Itera solo los que cumplen el filtro")
    void filtra() {
        var lista = List.of(plato(TipoComida.ALMUERZO), plato(TipoComida.CENA), plato(TipoComida.ALMUERZO));
        var it = new CatalogoIterator(lista, new FiltroTipoComida(TipoComida.ALMUERZO));

        int n = 0;
        while (it.hasNext()) { it.next(); n++; }
        assertEquals(2, n);
    }

    @Test
    @DisplayName("next() sin elementos lanza NoSuchElementException")
    void next_sinElementos() {
        var it = new CatalogoIterator(List.of(), null);
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    @DisplayName("Itera todos cuando filtro es null")
    void sinFiltro() {
        var lista = List.of(plato(TipoComida.ALMUERZO), plato(TipoComida.CENA));
        var it = new CatalogoIterator(lista, null);
        int n = 0;
        while (it.hasNext()) { it.next(); n++; }
        assertEquals(2, n);
    }
}