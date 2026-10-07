package com.ollacercana.service.moderacion;

import com.ollacercana.domain.Reporte;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
@Slf4j
public class RevisorAdministradorHandler extends AbstractModeracionReporteHandler {
    @Override
    protected void procesar(Reporte reporte) {
        log.info("Reporte {} encolado para revisión manual por un administrador.", reporte.getId());
                                                                                           
    }
}
