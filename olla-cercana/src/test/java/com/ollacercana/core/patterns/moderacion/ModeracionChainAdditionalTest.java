package com.ollacercana.core.patterns.moderacion;

import com.ollacercana.core.models.Reporte;
import com.ollacercana.core.models.enums.ObjetivoReporte;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ModeracionChainAdditionalTest {

    @Test
    @DisplayName("AbstractModeracionReporteHandler: encadena al siguiente")
    void chain_callsNext() {
        StringBuilder orden = new StringBuilder();
        AbstractModeracionReporteHandler a = new AbstractModeracionReporteHandler() {
            @Override protected void procesar(Reporte r) { orden.append("A"); }
        };
        AbstractModeracionReporteHandler b = new AbstractModeracionReporteHandler() {
            @Override protected void procesar(Reporte r) { orden.append("B"); }
        };
        a.setNext(b);
        a.handle(Reporte.builder().id(UUID.randomUUID()).build());
        assertEquals("AB", orden.toString());
    }

    @Test
    @DisplayName("Sin siguiente handler no falla")
    void chain_sinSiguiente() {
        AbstractModeracionReporteHandler a = new AbstractModeracionReporteHandler() {
            @Override protected void procesar(Reporte r) { }
        };
        assertDoesNotThrow(() -> a.handle(Reporte.builder().build()));
    }

    @Test
    @DisplayName("RevisorAdministradorHandler no falla")
    void revisor_noFalla() {
        new RevisorAdministradorHandler().procesar(Reporte.builder().id(UUID.randomUUID()).build());
    }

    @Test
    @DisplayName("ValidadorReporteHandler no falla")
    void validador_noFalla() {
        new ValidadorReporteHandler().procesar(Reporte.builder().id(UUID.randomUUID()).build());
    }

    @Test
    @DisplayName("EvaluadorReputacionHandler no falla")
    void evaluador_noFalla() {
        new EvaluadorReputacionHandler().procesar(Reporte.builder().id(UUID.randomUUID()).build());
    }
}