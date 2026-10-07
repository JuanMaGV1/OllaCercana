package com.ollacercana.service.moderacion;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.ObjetivoReporte;
import com.ollacercana.domain.Plato;
import com.ollacercana.domain.Reporte;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class OcultamientoPreventivoHandler extends AbstractModeracionReporteHandler {

    private final ReporteRepository reporteRepository;
    private final PlatoRepository platoRepository;

    @Override
    protected void procesar(Reporte reporte) {
        if (reporte.getObjetivo() == ObjetivoReporte.PLATO) {
            long reportantesDistintos = reporteRepository.countDistinctReportanteIdByObjetivoIdAndObjetivo(
                    reporte.getObjetivoId(), ObjetivoReporte.PLATO);

            if (reportantesDistintos >= 3) {
                platoRepository.findById(reporte.getObjetivoId()).ifPresent(plato -> {
                    if (plato.getEstado() != EstadoPlato.OCULTO) {
                        log.warn("Ocultamiento preventivo aplicado al plato {} por recibir {} reportes distintos",
                                plato.getId(), reportantesDistintos);
                        plato.setEstado(EstadoPlato.OCULTO);
                        platoRepository.save(plato);
                    }
                });
            }
        }
    }
}
