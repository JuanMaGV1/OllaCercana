package com.ollacercana.validator;

import com.ollacercana.domain.ObjetivoReporte;
import com.ollacercana.domain.Plato;
import com.ollacercana.dto.ReporteCrearDto;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReporteValidator {

    private final PlatoRepository platoRepository;
    private final CuentaRepository cuentaRepository;
    private final ReporteRepository reporteRepository;

    public void validarParaCreacion(ReporteCrearDto dto, UUID reportanteId) {
        if (dto.getMotivo() == null) {
            throw new ReglaDeNegocioException("Debe seleccionar un motivo de reporte");
        }

        if (dto.getObjetivo() == ObjetivoReporte.PLATO) {
            Plato plato = platoRepository.findById(dto.getObjetivoId())
                    .orElseThrow(() -> new ReglaDeNegocioException("El plato reportado no existe"));
            if (plato.getCocineraId().equals(reportanteId)) {
                throw new ReglaDeNegocioException("No puede auto-reportar su propio plato");
            }
        } else if (dto.getObjetivo() == ObjetivoReporte.CUENTA) {
            if (!cuentaRepository.existsById(dto.getObjetivoId())) {
                throw new ReglaDeNegocioException("La cuenta reportada no existe");
            }
            if (dto.getObjetivoId().equals(reportanteId)) {
                throw new ReglaDeNegocioException("No puede auto-reportarse");
            }
        }

        if (reporteRepository.existsByReportanteIdAndObjetivoIdAndObjetivo(reportanteId, dto.getObjetivoId(), dto.getObjetivo())) {
            throw new ReglaDeNegocioException("Ya ha reportado este objetivo anteriormente");
        }
    }
}
