package com.ollacercana.core.patterns.moderacion;

import com.ollacercana.core.models.Reporte;

public abstract class AbstractModeracionReporteHandler implements ModeracionReporteHandler {
    
    private ModeracionReporteHandler next;

    @Override
    public void setNext(ModeracionReporteHandler next) {
        this.next = next;
    }

    @Override
    public void handle(Reporte reporte) {
        procesar(reporte);
        if (next != null) {
            next.handle(reporte);
        }
    }

    protected abstract void procesar(Reporte reporte);
}