package com.ollacercana.mapper;

import com.ollacercana.domain.Reporte;
import com.ollacercana.dto.ReporteDto;
import org.springframework.stereotype.Component;

@Component
public class ReporteMapper {

    public ReporteDto toDto(Reporte reporte) {
        if (reporte == null) {
            return null;
        }

        return ReporteDto.builder()
                .id(reporte.getId())
                .objetivo(reporte.getObjetivo())
                .objetivoId(reporte.getObjetivoId())
                .cuentaObjetivoId(reporte.getCuentaObjetivoId())
                .motivo(reporte.getMotivo())
                .descripcion(reporte.getDescripcion())
                .evidencias(reporte.getEvidencias())
                .reportanteId(reporte.getReportanteId())
                .reservaId(reporte.getReservaId())
                .estado(reporte.getEstado())
                .fechaCreacion(reporte.getFechaCreacion())
                .build();
    }
}
