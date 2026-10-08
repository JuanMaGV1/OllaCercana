package com.ollacercana.core.patterns.moderacion;

import com.ollacercana.core.models.Reporte;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Slf4j
public class ValidadorReporteHandler extends AbstractModeracionReporteHandler {
    @Override
    protected void procesar(Reporte reporte) {
        log.info("Validando reporte {} contra reglas de moderación automáticas...", reporte.getId());
    }
}