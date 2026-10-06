package com.ollacercana.service;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.MotivoReporte;
import com.ollacercana.domain.ObjetivoReporte;
import com.ollacercana.domain.Plato;
import com.ollacercana.domain.Reporte;
import com.ollacercana.dto.ReporteCrearDto;
import com.ollacercana.dto.ReporteDto;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.mapper.ReporteMapper;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReporteRepository;
import com.ollacercana.service.moderacion.ModeracionReporteChainConfig;
import com.ollacercana.service.moderacion.ModeracionReporteHandler;
import com.ollacercana.validator.ReporteValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private PlatoRepository platoRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private ReporteMapper reporteMapper;

    @Mock
    private ModeracionReporteChainConfig moderacionChain;

    @Mock
    private ModeracionReporteHandler chainHandler;

    private ReporteValidator reporteValidator;
    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        reporteValidator = new ReporteValidator(platoRepository, cuentaRepository, reporteRepository);
        reporteService = new ReporteService(reporteRepository, reporteValidator, reporteMapper, moderacionChain);
    }

    @Test
    void testCreacionExitosa() {
        UUID reportanteId = UUID.randomUUID();
        UUID objetivoId = UUID.randomUUID();
        UUID cocineraId = UUID.randomUUID();
        
        ReporteCrearDto dto = ReporteCrearDto.builder()
                .objetivo(ObjetivoReporte.PLATO)
                .objetivoId(objetivoId)
                .motivo(MotivoReporte.CONTENIDO_INAPROPIADO)
                .build();

        Plato plato = new Plato();
        plato.setId(objetivoId);
        plato.setCocineraId(cocineraId);
        plato.setEstado(EstadoPlato.ACTIVO);

        when(platoRepository.findById(objetivoId)).thenReturn(Optional.of(plato));
        when(reporteRepository.existsByReportanteIdAndObjetivoIdAndObjetivo(reportanteId, objetivoId, ObjetivoReporte.PLATO))
                .thenReturn(false);
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(i -> i.getArgument(0));
        when(moderacionChain.getChain()).thenReturn(chainHandler);
        when(reporteMapper.toDto(any(Reporte.class))).thenReturn(new ReporteDto());

        ReporteDto result = reporteService.crear(dto, reportanteId);

        assertNotNull(result);
        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(chainHandler, times(1)).handle(any(Reporte.class));
    }

    @Test
    void testFalloPorFaltaDeMotivo() {
        UUID reportanteId = UUID.randomUUID();
        ReporteCrearDto dto = ReporteCrearDto.builder()
                .objetivo(ObjetivoReporte.PLATO)
                .objetivoId(UUID.randomUUID())
                .build(); // Sin motivo

        ReglaDeNegocioException exception = assertThrows(ReglaDeNegocioException.class, () -> 
                reporteService.crear(dto, reportanteId));

        assertEquals("Debe seleccionar un motivo de reporte", exception.getMessage());
    }

    @Test
    void testDeteccionReporteDuplicado() {
        UUID reportanteId = UUID.randomUUID();
        UUID objetivoId = UUID.randomUUID();
        
        ReporteCrearDto dto = ReporteCrearDto.builder()
                .objetivo(ObjetivoReporte.PLATO)
                .objetivoId(objetivoId)
                .motivo(MotivoReporte.FRAUDE_O_ESTAFA)
                .build();

        Plato plato = new Plato();
        plato.setId(objetivoId);
        plato.setCocineraId(UUID.randomUUID());

        when(platoRepository.findById(objetivoId)).thenReturn(Optional.of(plato));
        when(reporteRepository.existsByReportanteIdAndObjetivoIdAndObjetivo(reportanteId, objetivoId, ObjetivoReporte.PLATO))
                .thenReturn(true);

        ReglaDeNegocioException exception = assertThrows(ReglaDeNegocioException.class, () -> 
                reporteService.crear(dto, reportanteId));

        assertEquals("Ya ha reportado este objetivo anteriormente", exception.getMessage());
    }
}
