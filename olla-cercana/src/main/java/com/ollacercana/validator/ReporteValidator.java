package com.ollacercana.validator;

import com.ollacercana.domain.ObjetivoReporte;
import com.ollacercana.domain.Plato;
import com.ollacercana.dto.ReporteCrearDto;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReporteValidator {

    private final PlatoRepository platoRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;
    private final CuentaRepository cuentaRepository;
    private final ReporteRepository reporteRepository;

    public void validarParaCreacion(ReporteCrearDto dto, Long reportanteId) {
        if (dto == null) throw new ReglaDeNegocioException("El reporte es obligatorio");
        if (dto.getMotivo() == null) {
            throw new ReglaDeNegocioException("Debe seleccionar un motivo de reporte");
        }
        if (dto.getObjetivo() == null) {
            throw new ReglaDeNegocioException("Debe seleccionar el objetivo del reporte");
        }
        if (reportanteId == null) throw new ReglaDeNegocioException("No se pudo identificar al reportante");

        if (dto.getObjetivo() == ObjetivoReporte.PLATO) {
            if (dto.getObjetivoId() == null) throw new ReglaDeNegocioException("Debe seleccionar el objetivo del reporte");
            Plato plato = platoRepository.findById(dto.getObjetivoId())
                    .orElseThrow(() -> new ReglaDeNegocioException("El plato reportado no existe"));
            boolean propio = plato.getCocineraId() != null && perfilCocineraRepository
                    .findByCuentaId(reportanteId)
                    .map(perfil -> perfil.getId().equals(plato.getCocineraId()))
                    .orElse(false);
            if (propio) throw new ReglaDeNegocioException("No puede reportar su propia publicación");
        } else if (dto.getObjetivo() == ObjetivoReporte.CUENTA) {
            if (dto.getCuentaObjetivoId() != null && dto.getObjetivoId() != null) {
                throw new ReglaDeNegocioException("Debe indicar un único usuario objetivo");
            }
            Long cuentaObjetivoId = dto.getCuentaObjetivoId();
            if (cuentaObjetivoId == null && dto.getObjetivoId() != null) {
                cuentaObjetivoId = perfilCocineraRepository.findById(dto.getObjetivoId())
                        .map(perfil -> perfil.getCuenta() == null ? null : perfil.getCuenta().getId())
                        .orElse(null);
            }
            if (cuentaObjetivoId == null || !cuentaRepository.existsById(cuentaObjetivoId)) {
                throw new ReglaDeNegocioException("La cuenta reportada no existe");
            }
            if (reportanteId.equals(cuentaObjetivoId)) {
                throw new ReglaDeNegocioException("No puede reportar su propia cuenta");
            }
            boolean duplicadoCuenta = reporteRepository.existsByReportanteIdAndCuentaObjetivoIdAndObjetivo(
                    reportanteId, cuentaObjetivoId, ObjetivoReporte.CUENTA);
            boolean duplicadoUuid = dto.getCuentaObjetivoId() == null && dto.getObjetivoId() != null && reporteRepository
                    .existsByReportanteIdAndObjetivoIdAndObjetivo(reportanteId, dto.getObjetivoId(), ObjetivoReporte.CUENTA);
            if (duplicadoCuenta || duplicadoUuid) {
                throw new ReglaDeNegocioException("Ya ha reportado este objetivo anteriormente");
            }
            return;
        }

        if (dto.getObjetivo() == ObjetivoReporte.PLATO && reporteRepository.existsByReportanteIdAndObjetivoIdAndObjetivo(
                reportanteId, dto.getObjetivoId(), dto.getObjetivo())) {
            throw new ReglaDeNegocioException("Ya ha reportado este objetivo anteriormente");
        }
    }
}
