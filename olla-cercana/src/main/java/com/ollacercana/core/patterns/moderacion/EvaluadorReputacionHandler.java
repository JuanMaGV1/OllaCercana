package com.ollacercana.core.patterns.moderacion;

import com.ollacercana.core.models.Reporte;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@Slf4j
public class EvaluadorReputacionHandler extends AbstractModeracionReporteHandler {
    @Override
    protected void procesar(Reporte reporte) {
        log.info("Evaluando impacto en reputación por el reporte {}...", reporte.getId());
    }
}