package com.ollacercana.core.patterns.iterator;

import com.ollacercana.core.models.Plato;
import com.ollacercana.core.patterns.filter.FiltroPlato;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * OC-006 / OC-007 / OC-008: Iterator sobre el catálogo de platos.
 * Recorre la colección aplicando el filtro (normalmente Composite) sin exponer
 * la estructura interna.
 */
public class CatalogoIterator implements Iterator<Plato> {

    private final Iterator<Plato> interno;
    private final FiltroPlato filtro;
    private Plato siguiente;
    private boolean listo = false;

    public CatalogoIterator(List<Plato> platos, FiltroPlato filtro) {
        this.interno = platos.iterator();
        this.filtro = filtro;
    }

    @Override
    public boolean hasNext() {
        if (listo) return siguiente != null;
        listo = true;
        while (interno.hasNext()) {
            Plato candidato = interno.next();
            if (filtro == null || filtro.cumple(candidato)) {
                siguiente = candidato;
                return true;
            }
        }
        siguiente = null;
        return false;
    }

    @Override
    public Plato next() {
        if (!hasNext()) throw new NoSuchElementException();
        listo = false;
        return siguiente;
    }
}