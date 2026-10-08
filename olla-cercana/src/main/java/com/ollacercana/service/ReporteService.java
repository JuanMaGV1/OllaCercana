package com.ollacercana.service;

import com.ollacercana.domain.EstadoReporte;
import com.ollacercana.domain.Reporte;
import com.ollacercana.dto.ReporteCrearDto;
import com.ollacercana.dto.ReporteDto;
import com.ollacercana.mapper.ReporteMapper;
import com.ollacercana.repository.ReporteRepository;
import com.ollacercana.service.moderacion.ModeracionReporteChainConfig;
import com.ollacercana.validator.ReporteValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final ReporteValidator reporteValidator;
    private final ReporteMapper reporteMapper;
    private final ModeracionReporteChainConfig moderacionChain;

    @Transactional
    public ReporteDto crear(ReporteCrearDto dto, Long reportanteId) {
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

        reporte = reporteRepository.save(reporte);

                                        
        moderacionChain.getChain().handle(reporte);

        return reporteMapper.toDto(reporte);
    }
}
