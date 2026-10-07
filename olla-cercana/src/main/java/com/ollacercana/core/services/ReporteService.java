package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.request.ReporteCrearDTO;
import com.ollacercana.controller.dtos.response.ReporteDTO;
import com.ollacercana.controller.handlers.exception.ReglaDeNegocioException;
import com.ollacercana.controller.mappers.ReporteMapper;
import com.ollacercana.core.models.Reporte;
import com.ollacercana.core.models.enums.EstadoReporte;
import com.ollacercana.core.patterns.moderacion.ModeracionReporteChainConfig;
import com.ollacercana.core.validators.ReporteValidator;
import com.ollacercana.persistence.entities.ReporteEntity;
import com.ollacercana.persistence.mappers.ReporteEntityMapper;
import com.ollacercana.persistence.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final ReporteValidator reporteValidator;
    private final ReporteEntityMapper reporteEntityMapper;
    private final ReporteMapper reporteMapper;
    private final ModeracionReporteChainConfig moderacionChain;

    @Transactional
    public ReporteDTO crear(ReporteCrearDTO dto, Long reportanteId) {
        reporteValidator.validarParaCreacion(dto, reportanteId);

        Reporte reporte = Reporte.builder()
                .objetivo(dto.getObjetivo())
                .objetivoId(dto.getObjetivoId())
                .cuentaObjetivoId(dto.getCuentaObjetivoId())
                .motivo(dto.getMotivo())
                .descripcion(dto.getDescripcion())
                .evidencias(dto.getEvidencias())
                .reportanteId(reportanteId)
                .reservaId(dto.getReservaId())
                .estado(EstadoReporte.ABIERTO)
                .fechaCreacion(LocalDateTime.now())
                .build();

        ReporteEntity guardado = reporteRepository.save(reporteEntityMapper.toEntity(reporte));
        Reporte dominioGuardado = reporteEntityMapper.toDomain(guardado);
        moderacionChain.getChain().handle(dominioGuardado);
        return reporteMapper.toDto(dominioGuardado);
    }
}