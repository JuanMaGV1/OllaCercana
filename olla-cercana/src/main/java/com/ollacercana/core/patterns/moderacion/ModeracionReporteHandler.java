package com.ollacercana.core.patterns.moderacion;

import com.ollacercana.core.models.Reporte;

public interface ModeracionReporteHandler {
    
    void setNext(ModeracionReporteHandler next);
    
    void handle(Reporte reporte);
}