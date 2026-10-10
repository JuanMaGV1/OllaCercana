package com.ollacercana.core.patterns.moderacion;

import com.ollacercana.core.models.Reporte;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.ObjetivoReporte;
import com.ollacercana.persistence.mappers.PlatoEntityMapper;
import com.ollacercana.persistence.repository.PlatoRepository;
import com.ollacercana.persistence.repository.ReporteRepository;
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
    private final PlatoEntityMapper platoEntityMapper;

    @Override
    protected void procesar(Reporte reporte) {
        if (reporte.getObjetivo() == ObjetivoReporte.PLATO) {
            long reportantesDistintos = reporteRepository.countDistinctReportanteIdByObjetivoIdAndObjetivo(
                    reporte.getObjetivoId(), ObjetivoReporte.PLATO);

            if (reportantesDistintos >= 3) {
                platoRepository.findById(reporte.getObjetivoId()).ifPresent(platoEntity -> {
                    if (platoEntity.getEstado() != EstadoPlato.OCULTO) {
                        log.warn("Ocultamiento preventivo aplicado al plato {} por {} reportes distintos",
                                platoEntity.getId(), reportantesDistintos);
                        platoEntity.setEstado(EstadoPlato.OCULTO);
                        platoRepository.save(platoEntity);
                    }
                });
            }
        }
    }
}