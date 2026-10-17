package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.response.CalificacionResponseDTO;
import com.ollacercana.controller.dtos.response.PaginaResponseDTO;
import com.ollacercana.controller.handlers.exception.ResourceNotFoundException;
import com.ollacercana.controller.mappers.CalificacionMapper;
import com.ollacercana.core.models.Calificacion;
import com.ollacercana.core.models.Reporte;
import com.ollacercana.core.models.enums.EstadoCalificacion;
import com.ollacercana.core.models.enums.EstadoReporte;
import com.ollacercana.core.patterns.moderacion.EvaluadorReputacionCalificacion;
import com.ollacercana.core.services.impl.CalificacionServiceImpl;
import com.ollacercana.core.services.impl.ModeracionServiceImpl;
import com.ollacercana.core.validators.CalificacionValidator;
import com.ollacercana.persistence.entities.CalificacionEntity;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.entities.ReporteEntity;
import com.ollacercana.persistence.mappers.*;
import com.ollacercana.persistence.repository.*;
import com.ollacercana.persistence.repository.mongo.NotificacionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalificacionYModeracionRamasTest {

    // ──────────────── CalificacionServiceImpl ────────────────
    @Mock private CalificacionRepository calificacionRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private PerfilCocineraRepository perfilRepository;
    @Mock private CalificacionEntityMapper calificacionEntityMapper;
    @Mock private ReservaEntityMapper reservaEntityMapper;
    @Mock private CalificacionMapper calificacionMapper;
    @Mock private CalificacionValidator calificacionValidator;
    @Mock private EvaluadorReputacionCalificacion evaluadorReputacion;

    @InjectMocks private CalificacionServiceImpl calificacionService;

    // ──────────────── ModeracionServiceImpl ────────────────
    @Mock private ReporteRepository reporteRepository;
    @Mock private DecisionModeracionRepository decisionRepository;
    @Mock private PlatoRepository platoRepository;
    @Mock private CuentaRepository cuentaRepository;
    @Mock private NotificacionRepository notificacionRepository;
    @Mock private ReporteEntityMapper reporteMapper;
    @Mock private DecisionModeracionEntityMapper decisionMapper;
    @Mock private PlatoEntityMapper platoMapper;
    @Mock private CuentaEntityMapper cuentaMapper;
    @Mock private PerfilCocineraDomainMapper perfilDomainMapper;
    @Mock private NotificacionDocumentMapper notificacionMapper;

    @InjectMocks private ModeracionServiceImpl moderacionService;

    @Test
    @DisplayName("CalificacionService: listarPorCocinera con y sin filtro de estrellas")
    void calificacionService_listarPorCocinera() {
        UUID cocineraId = UUID.randomUUID();
        CalificacionEntity entity = CalificacionEntity.builder().id(UUID.randomUUID()).estrellas(5).build();
        Page<CalificacionEntity> page = new PageImpl<>(List.of(entity));

        when(calificacionRepository.findByCocineraIdAndEstrellas(eq(cocineraId), eq(EstadoCalificacion.PUBLICADA), eq(5), any(Pageable.class)))
                .thenReturn(page);
        when(calificacionEntityMapper.toDomain(entity)).thenReturn(Calificacion.builder().estrellas(5).build());
        when(calificacionMapper.toResponse(any(Calificacion.class))).thenReturn(CalificacionResponseDTO.builder().estrellas(5).build());

        PaginaResponseDTO<CalificacionResponseDTO> resp = calificacionService.listarPorCocinera(cocineraId, 5, 0, 10);
        assertEquals(1, resp.getContenido().size());

        when(calificacionRepository.findByCocineraId(eq(cocineraId), eq(EstadoCalificacion.PUBLICADA), any(Pageable.class)))
                .thenReturn(page);
        PaginaResponseDTO<CalificacionResponseDTO> respSinFiltro = calificacionService.listarPorCocinera(cocineraId, null, 0, 10);
        assertEquals(1, respSinFiltro.getContenido().size());
    }

    @Test
    @DisplayName("CalificacionService: publicarPendientesVencidas publica calificaciones vencidas y actualiza perfil")
    void calificacionService_publicarPendientesVencidas() {
        LocalDateTime ahora = LocalDateTime.now();
        UUID cocineraId = UUID.randomUUID();
        CalificacionEntity entity = CalificacionEntity.builder().id(UUID.randomUUID()).cocineraId(cocineraId).build();
        Calificacion cal = Calificacion.builder().cocineraId(cocineraId).estado(EstadoCalificacion.PENDIENTE).build();

        when(calificacionRepository.findByEstadoAndFechaLimitePublicacionLessThanEqual(EstadoCalificacion.PENDIENTE, ahora))
                .thenReturn(List.of(entity));
        when(calificacionEntityMapper.toDomain(entity)).thenReturn(cal);
        when(calificacionEntityMapper.toEntity(cal)).thenReturn(entity);
        when(calificacionRepository.save(entity)).thenReturn(entity);
        when(perfilRepository.findById(cocineraId)).thenReturn(Optional.of(PerfilCocineraEntity.builder().id(cocineraId).build()));
        when(calificacionRepository.promedioPorCocinera(cocineraId)).thenReturn(4.6);
        when(calificacionRepository.contarPositivas(cocineraId)).thenReturn(55L);

        int count = calificacionService.publicarPendientesVencidas(ahora);

        assertEquals(1, count);
        verify(calificacionRepository).save(entity);
        verify(perfilRepository).save(any(PerfilCocineraEntity.class));
    }

    // ──────────────── ModeracionService ────────────────

    @Test
    @DisplayName("ModeracionService: obtenerReportes con y sin filtro de estado")
    void moderacionService_obtenerReportes() {
        ReporteEntity entity = ReporteEntity.builder().id(UUID.randomUUID()).estado(EstadoReporte.ABIERTO).build();
        Page<ReporteEntity> page = new PageImpl<>(List.of(entity));

        when(reporteRepository.findByEstado(eq(EstadoReporte.ABIERTO), any(Pageable.class))).thenReturn(page);
        when(reporteMapper.toDomain(entity)).thenReturn(Reporte.builder().id(entity.getId()).build());

        Page<Reporte> resConFiltro = moderacionService.obtenerReportes(EstadoReporte.ABIERTO, PageRequest.of(0, 10));
        assertEquals(1, resConFiltro.getContent().size());

        when(reporteRepository.findAll(any(Pageable.class))).thenReturn(page);
        Page<Reporte> resSinFiltro = moderacionService.obtenerReportes(null, PageRequest.of(0, 10));
        assertEquals(1, resSinFiltro.getContent().size());
    }

    @Test
    @DisplayName("ModeracionService: obtenerDetalle lanza 404 si no existe")
    void moderacionService_obtenerDetalle_notFound() {
        UUID id = UUID.randomUUID();
        when(reporteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> moderacionService.obtenerDetalle(id));
    }

    @Test
    @DisplayName("ModeracionService: listarPerfilesPausados")
    void moderacionService_listarPerfilesPausados() {
        PerfilCocineraEntity perfil = PerfilCocineraEntity.builder().id(UUID.randomUUID()).pausada(true).build();
        when(perfilRepository.findByPausadaTrue()).thenReturn(List.of(perfil));
        when(perfilDomainMapper.toDomain(perfil)).thenReturn(com.ollacercana.core.models.PerfilCocinera.builder().build());

        var lista = moderacionService.listarPerfilesPausados();
        assertEquals(1, lista.size());
    }
}