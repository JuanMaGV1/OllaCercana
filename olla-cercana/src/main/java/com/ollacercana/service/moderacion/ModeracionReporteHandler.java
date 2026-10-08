package com.ollacercana.service.moderacion;

import com.ollacercana.domain.Reporte;

public interface ModeracionReporteHandler {
    void setNext(ModeracionReporteHandler next);
    void handle(Reporte reporte);
}
