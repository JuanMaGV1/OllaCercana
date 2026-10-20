package com.ollacercana.core.validators;

import com.ollacercana.controller.dtos.request.ReporteCrearDTO;
import com.ollacercana.controller.handlers.exception.ReglaDeNegocioException;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.ObjetivoReporte;
import com.ollacercana.persistence.mappers.PlatoEntityMapper;
import com.ollacercana.persistence.repository.CuentaRepository;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.PlatoRepository;
import com.ollacercana.persistence.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ReporteValidator {

    private final PlatoRepository platoRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;
    private final CuentaRepository cuentaRepository;
    private final ReporteRepository reporteRepository;
    private final PlatoEntityMapper platoEntityMapper;

    public void validarParaCreacion(ReporteCrearDTO dto, Long reportanteId) {
        validarParametrosBasicos(dto, reportanteId);

        if (dto.getObjetivo() == ObjetivoReporte.PLATO) {
            validarReportePlato(dto, reportanteId);
        } else if (dto.getObjetivo() == ObjetivoReporte.CUENTA) {
            validarReporteCuenta(dto, reportanteId);
        }
    }

    private void validarParametrosBasicos(ReporteCrearDTO dto, Long reportanteId) {
        if (dto == null) throw new ReglaDeNegocioException("El reporte es obligatorio");
        if (dto.getMotivo() == null) throw new ReglaDeNegocioException("Debe seleccionar un motivo de reporte");
        if (dto.getObjetivo() == null) throw new ReglaDeNegocioException("Debe seleccionar el objetivo del reporte");
        if (reportanteId == null) throw new ReglaDeNegocioException("No se pudo identificar al reportante");
    }

    private void validarReportePlato(ReporteCrearDTO dto, Long reportanteId) {
        if (dto.getObjetivoId() == null) {
            throw new ReglaDeNegocioException("Debe seleccionar el objetivo del reporte");
        }
        Plato plato = platoRepository.findById(dto.getObjetivoId())
                .map(platoEntityMapper::toDomain)
                .orElseThrow(() -> new ReglaDeNegocioException("El plato reportado no existe"));

        boolean propio = plato.getCocineraId() != null && perfilCocineraRepository
                .findByCuentaId(reportanteId)
                .map(perfil -> perfil.getId().equals(plato.getCocineraId()))
                .orElse(false);

        if (propio) {
            throw new ReglaDeNegocioException("No puede reportar su propia publicación");
        }

        if (reporteRepository.existsByReportanteIdAndObjetivoIdAndObjetivo(
                reportanteId, dto.getObjetivoId(), dto.getObjetivo())) {
            throw new ReglaDeNegocioException("Ya ha reportado este objetivo anteriormente");
        }
    }

    private void validarReporteCuenta(ReporteCrearDTO dto, Long reportanteId) {
        if (dto.getCuentaObjetivoId() != null && dto.getObjetivoId() != null) {
            throw new ReglaDeNegocioException("Debe indicar un único usuario objetivo");
        }
        Long cuentaObjetivoId = resolverCuentaObjetivoId(dto);

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
    }

    private Long resolverCuentaObjetivoId(ReporteCrearDTO dto) {
        if (dto.getCuentaObjetivoId() != null) {
            return dto.getCuentaObjetivoId();
        }
        if (dto.getObjetivoId() != null) {
            return perfilCocineraRepository.findById(dto.getObjetivoId())
                    .map(perfil -> perfil.getCuenta() == null ? null : perfil.getCuenta().getId())
                    .orElse(null);
        }
        return null;
    }
}