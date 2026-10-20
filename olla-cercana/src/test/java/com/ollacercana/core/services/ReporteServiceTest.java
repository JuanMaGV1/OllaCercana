package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.request.ReporteCrearDTO;
import com.ollacercana.controller.dtos.response.ReporteDTO;
import com.ollacercana.controller.handlers.exception.ReglaDeNegocioException;
import com.ollacercana.controller.mappers.ReporteMapper;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.Reporte;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.MotivoReporte;
import com.ollacercana.core.models.enums.ObjetivoReporte;
import com.ollacercana.core.patterns.moderacion.ModeracionReporteChainConfig;
import com.ollacercana.core.patterns.moderacion.ModeracionReporteHandler;
import com.ollacercana.core.services.impl.ReporteServiceImpl;
import com.ollacercana.core.validators.ReporteValidator;
import com.ollacercana.persistence.entities.ReporteEntity;
import com.ollacercana.persistence.mappers.PlatoEntityMapper;
import com.ollacercana.persistence.mappers.ReporteEntityMapper;
import com.ollacercana.persistence.repository.CuentaRepository;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.PlatoRepository;
import com.ollacercana.persistence.repository.ReporteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock private ReporteRepository reporteRepository;
    @Mock private PlatoRepository platoRepository;
    @Mock private PerfilCocineraRepository perfilCocineraRepository;
    @Mock private CuentaRepository cuentaRepository;
    @Mock private ReporteEntityMapper reporteEntityMapper;
    @Mock private PlatoEntityMapper platoEntityMapper;
    @Mock private ReporteMapper reporteMapper;
    @Mock private ModeracionReporteChainConfig moderacionChain;
    @Mock private ModeracionReporteHandler chainHandler;

    private ReporteServiceImpl reporteService;

    @BeforeEach
    void setUp() {
        ReporteValidator reporteValidator = new ReporteValidator(
                platoRepository, perfilCocineraRepository, cuentaRepository,
                reporteRepository, platoEntityMapper);
        reporteService = new ReporteServiceImpl(
                reporteRepository, reporteValidator, reporteEntityMapper,
                reporteMapper, moderacionChain);
    }

    @Test
    @DisplayName("Crear reporte exitoso → guarda entidad, invoca chain, retorna DTO")
    void testCreacionExitosa() {
        Long reportanteId = 42L;
        UUID objetivoId = UUID.randomUUID();
        UUID cocineraId = UUID.randomUUID();

        ReporteCrearDTO dto = ReporteCrearDTO.builder()
                .objetivo(ObjetivoReporte.PLATO)
                .objetivoId(objetivoId)
                .motivo(MotivoReporte.CONTENIDO_INAPROPIADO)
                .build();

        Plato plato = Plato.builder()
                .id(objetivoId)
                .cocineraId(cocineraId)
                .estado(EstadoPlato.ACTIVO)
                .build();

        when(platoRepository.findById(objetivoId))
                .thenReturn(Optional.of(com.ollacercana.persistence.entities.PlatoEntity.builder()
                        .id(objetivoId).cocineraId(cocineraId).build()));
        when(platoEntityMapper.toDomain(any())).thenReturn(plato);
        when(perfilCocineraRepository.findByCuentaId(reportanteId)).thenReturn(Optional.empty());
        when(reporteRepository.existsByReportanteIdAndObjetivoIdAndObjetivo(
                reportanteId, objetivoId, ObjetivoReporte.PLATO)).thenReturn(false);

        ReporteEntity guardadaEntity = ReporteEntity.builder().id(UUID.randomUUID()).build();
        when(reporteEntityMapper.toEntity(any(Reporte.class))).thenReturn(guardadaEntity);
        when(reporteRepository.save(guardadaEntity)).thenReturn(guardadaEntity);
        when(reporteEntityMapper.toDomain(guardadaEntity)).thenReturn(Reporte.builder().id(UUID.randomUUID()).build());
        when(moderacionChain.getChain()).thenReturn(chainHandler);
        when(reporteMapper.toDto(any(Reporte.class))).thenReturn(new ReporteDTO());

        ReporteDTO result = reporteService.crear(dto, reportanteId);

        assertNotNull(result);
        verify(reporteRepository).save(any(ReporteEntity.class));
        verify(chainHandler).handle(any(Reporte.class));
    }

    @Test
    @DisplayName("Sin motivo → ReglaDeNegocioException")
    void testFalloPorFaltaDeMotivo() {
        Long reportanteId = 42L;
        ReporteCrearDTO dto = ReporteCrearDTO.builder()
                .objetivo(ObjetivoReporte.PLATO)
                .objetivoId(UUID.randomUUID())
                .build();

        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class,
                () -> reporteService.crear(dto, reportanteId));

        assertEquals("Debe seleccionar un motivo de reporte", ex.getMessage());
    }

    @Test
    @DisplayName("Reporte duplicado → ReglaDeNegocioException")
    void testDeteccionReporteDuplicado() {
        Long reportanteId = 42L;
        UUID objetivoId = UUID.randomUUID();

        ReporteCrearDTO dto = ReporteCrearDTO.builder()
                .objetivo(ObjetivoReporte.PLATO)
                .objetivoId(objetivoId)
                .motivo(MotivoReporte.FRAUDE_O_ESTAFA)
                .build();

        Plato plato = Plato.builder()
                .id(objetivoId)
                .cocineraId(UUID.randomUUID())
                .build();

        when(platoRepository.findById(objetivoId))
                .thenReturn(Optional.of(com.ollacercana.persistence.entities.PlatoEntity.builder()
                        .id(objetivoId).build()));
        when(platoEntityMapper.toDomain(any())).thenReturn(plato);
        when(perfilCocineraRepository.findByCuentaId(reportanteId)).thenReturn(Optional.empty());
        when(reporteRepository.existsByReportanteIdAndObjetivoIdAndObjetivo(
                reportanteId, objetivoId, ObjetivoReporte.PLATO)).thenReturn(true);

        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class,
                () -> reporteService.crear(dto, reportanteId));

        assertEquals("Ya ha reportado este objetivo anteriormente", ex.getMessage());
    }

    @Test
    @DisplayName("Reporte directo de cuenta → guarda con cuentaObjetivoId")
    void testReporteDirectoDeCuenta() {
        Long reportanteId = 42L;
        Long cuentaObjetivoId = 84L;

        ReporteCrearDTO dto = ReporteCrearDTO.builder()
                .objetivo(ObjetivoReporte.CUENTA)
                .cuentaObjetivoId(cuentaObjetivoId)
                .motivo(MotivoReporte.COMPORTAMIENTO_OFENSIVO)
                .build();

        when(cuentaRepository.existsById(cuentaObjetivoId)).thenReturn(true);
        when(reporteRepository.existsByReportanteIdAndCuentaObjetivoIdAndObjetivo(
                reportanteId, cuentaObjetivoId, ObjetivoReporte.CUENTA)).thenReturn(false);

        ReporteEntity guardadaEntity = ReporteEntity.builder().id(UUID.randomUUID()).build();
        when(reporteEntityMapper.toEntity(any(Reporte.class))).thenReturn(guardadaEntity);
        when(reporteRepository.save(any(ReporteEntity.class))).thenReturn(guardadaEntity);
        when(reporteEntityMapper.toDomain(any(ReporteEntity.class))).thenReturn(Reporte.builder().build());
        when(moderacionChain.getChain()).thenReturn(chainHandler);
        when(reporteMapper.toDto(any(Reporte.class))).thenReturn(new ReporteDTO());

        reporteService.crear(dto, reportanteId);

        verify(reporteEntityMapper).toEntity(argThat(r ->
                cuentaObjetivoId.equals(r.getCuentaObjetivoId()) && r.getObjetivoId() == null));
    }
}