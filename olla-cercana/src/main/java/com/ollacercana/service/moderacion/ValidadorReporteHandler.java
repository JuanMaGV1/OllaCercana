package com.ollacercana.service.moderacion;

import com.ollacercana.domain.Reporte;
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
        // En una implementación real, aquí se podrían integrar APIs de análisis de texto para la descripción.
    }
}
