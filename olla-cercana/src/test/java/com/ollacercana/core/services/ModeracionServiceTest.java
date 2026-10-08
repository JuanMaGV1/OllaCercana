package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.request.EjecutarDecisionDTO;
import com.ollacercana.controller.handlers.exception.ReglaDeNegocioException;
import com.ollacercana.core.models.*;
import com.ollacercana.core.models.enums.*;
import com.ollacercana.persistence.entities.*;
import com.ollacercana.persistence.mappers.*;
import com.ollacercana.persistence.repository.*;
import com.ollacercana.persistence.repository.mongo.NotificacionRepository;
import com.ollacercana.persistence.document.NotificacionDocument;
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
class ModeracionServiceTest {

    @Mock private ReporteRepository reporteRepository;
    @Mock private DecisionModeracionRepository decisionRepository;
    @Mock private PlatoRepository platoRepository;
    @Mock private CuentaRepository cuentaRepository;
    @Mock private PerfilCocineraRepository perfilRepository;
    @Mock private NotificacionRepository notificacionRepository;

    @Mock private ReporteEntityMapper reporteMapper;
    @Mock private DecisionModeracionEntityMapper decisionMapper;
    @Mock private PlatoEntityMapper platoMapper;
    @Mock private CuentaEntityMapper cuentaMapper;
    @Mock private PerfilCocineraDomainMapper perfilDomainMapper;
    @Mock private NotificacionDocumentMapper notificacionMapper;

    private ModeracionService service;

    private final UUID reporteId = UUID.randomUUID();
    private final UUID platoId = UUID.randomUUID();
    private final UUID cocineraId = UUID.randomUUID();
    private final Long adminId = 99L;

    @BeforeEach
    void setUp() {
        service = new ModeracionService(
                reporteRepository, decisionRepository, platoRepository,
                cuentaRepository, perfilRepository, notificacionRepository,
                reporteMapper, decisionMapper, platoMapper, cuentaMapper,
                perfilDomainMapper, notificacionMapper);
    }

    private Reporte reportePlato() {
        return Reporte.builder()
                .id(reporteId)
                .objetivo(ObjetivoReporte.PLATO)
                .objetivoId(platoId)
                .estado(EstadoReporte.ABIERTO)
                .build();
    }

    private PlatoEntity platoEntity() {
        return PlatoEntity.builder()
                .id(platoId)
                .cocineraId(cocineraId)
                .estado(EstadoPlato.ACTIVO)
                .porcionesTotales(5)
                .porcionesComprometidas(0)
                .build();
    }

    private void stubBasicos(Reporte reporte) {
        when(reporteRepository.findById(reporteId)).thenReturn(Optional.of(ReporteEntity.builder().id(reporteId).build()));
        when(reporteMapper.toDomain(any(ReporteEntity.class))).thenReturn(reporte);
        when(reporteMapper.toEntity(any(Reporte.class))).thenReturn(ReporteEntity.builder().id(reporteId).build());
        when(decisionMapper.toEntity(any(DecisionModeracion.class))).thenReturn(DecisionModeracionEntity.builder().build());
        when(notificacionMapper.toDocument(any(Notificacion.class)))
                .thenReturn(NotificacionDocument.builder().build());
    }

    // ============ INHABILITAR PUBLICACION ============

    @Test
    @DisplayName("INHABILITAR_PUBLICACION oculta el plato y marca el reporte como resuelto")
    void testResolver_InhabilitarPublicacion() {
        Reporte reporte = reportePlato();
        PlatoEntity plato = platoEntity();

        stubBasicos(reporte);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));
        when(platoRepository.save(any(PlatoEntity.class))).thenReturn(plato);

        EjecutarDecisionDTO dto = new EjecutarDecisionDTO();
        dto.setDecision(TipoDecision.INHABILITAR_PUBLICACION);
        dto.setJustificacion("Falta grave");

        service.resolver(reporteId, dto, adminId);

        assertEquals(EstadoPlato.OCULTO, plato.getEstado());
        verify(decisionRepository).save(any(DecisionModeracionEntity.class));
        verify(platoRepository).save(plato);
        verify(notificacionRepository).save(any(NotificacionDocument.class));
    }

    // ============ RESTAURAR PUBLICACION ============

    @Test
    @DisplayName("RESTAURAR_PUBLICACION con porciones disponibles deja el plato ACTIVO")
    void testResolver_RestaurarPublicacion() {
        Reporte reporte = reportePlato();
        PlatoEntity plato = platoEntity();
        plato.setEstado(EstadoPlato.OCULTO);

        stubBasicos(reporte);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));
        when(platoRepository.save(any(PlatoEntity.class))).thenReturn(plato);

        EjecutarDecisionDTO dto = new EjecutarDecisionDTO();
        dto.setDecision(TipoDecision.RESTAURAR_PUBLICACION);
        dto.setJustificacion("Falsa alarma");

        service.resolver(reporteId, dto, adminId);

        assertEquals(EstadoPlato.ACTIVO, plato.getEstado());
        verify(platoRepository).save(plato);
    }

    @Test
    @DisplayName("RESTAURAR_PUBLICACION sin porciones deja el plato AGOTADO")
    void testResolver_RestaurarPublicacionSinPorciones() {
        Reporte reporte = reportePlato();
        PlatoEntity plato = platoEntity();
        plato.setEstado(EstadoPlato.OCULTO);
        plato.setPorcionesTotales(5);
        plato.setPorcionesComprometidas(5);

        stubBasicos(reporte);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));
        when(platoRepository.save(any(PlatoEntity.class))).thenReturn(plato);

        EjecutarDecisionDTO dto = new EjecutarDecisionDTO();
        dto.setDecision(TipoDecision.RESTAURAR_PUBLICACION);
        dto.setJustificacion("Falsa alarma");

        service.resolver(reporteId, dto, adminId);

        assertEquals(EstadoPlato.AGOTADO, plato.getEstado());
    }

    // ============ DEScartar / ADVERTIR ============

    @Test
    @DisplayName("DESCARTAR no modifica el plato")
    void testResolver_Descartar() {
        Reporte reporte = reportePlato();
        PlatoEntity plato = platoEntity();

        stubBasicos(reporte);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        EjecutarDecisionDTO dto = new EjecutarDecisionDTO();
        dto.setDecision(TipoDecision.DESCARTAR);
        dto.setJustificacion("Falsa alarma");

        service.resolver(reporteId, dto, adminId);

        assertEquals(EstadoPlato.ACTIVO, plato.getEstado());
        verify(platoRepository, never()).save(any());
        verify(decisionRepository).save(any(DecisionModeracionEntity.class));
    }

    @Test
    @DisplayName("ADVERTIR no modifica el plato")
    void testResolver_Advertir() {
        Reporte reporte = reportePlato();
        PlatoEntity plato = platoEntity();

        stubBasicos(reporte);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        EjecutarDecisionDTO dto = new EjecutarDecisionDTO();
        dto.setDecision(TipoDecision.ADVERTIR);
        dto.setJustificacion("Cuidado con las porciones");

        service.resolver(reporteId, dto, adminId);

        assertEquals(EstadoPlato.ACTIVO, plato.getEstado());
        verify(platoRepository, never()).save(any());
    }

    // ============ SUSPENDER CUENTA ============

    @Test
    @DisplayName("SUSPENDER_CUENTA cambia el estado de la cuenta objetivo")
    void testResolver_SuspenderCuenta() {
        Reporte reporteCuenta = Reporte.builder()
                .id(reporteId)
                .objetivo(ObjetivoReporte.CUENTA)
                .objetivoId(cocineraId)
                .cuentaObjetivoId(100L)
                .estado(EstadoReporte.ABIERTO)
                .build();

        CuentaEntity cuenta = CuentaEntity.builder()
                .id(100L)
                .estado(EstadoCuenta.ACTIVO)
                .roles(java.util.Set.of(Rol.COMPRADOR))
                .build();

        stubBasicos(reporteCuenta);
        when(cuentaRepository.findById(100L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any(CuentaEntity.class))).thenReturn(cuenta);

        EjecutarDecisionDTO dto = new EjecutarDecisionDTO();
        dto.setDecision(TipoDecision.SUSPENDER_CUENTA);
        dto.setJustificacion("Violaciones repetidas");

        service.resolver(reporteId, dto, adminId);

        assertEquals(EstadoCuenta.SUSPENDIDO, cuenta.getEstado());
        verify(cuentaRepository).save(cuenta);
    }

    // ============ REACTIVAR PERFIL ============

    @Test
    @DisplayName("reactivarPerfil con perfil pausado funciona")
    void testReactivarPerfil_Exito() {
        PerfilCocineraEntity perfil = PerfilCocineraEntity.builder()
                .id(cocineraId)
                .pausada(true)
                .build();

        when(perfilRepository.findById(cocineraId)).thenReturn(Optional.of(perfil));
        when(perfilRepository.save(any(PerfilCocineraEntity.class))).thenReturn(perfil);
        when(decisionMapper.toEntity(any(DecisionModeracion.class))).thenReturn(DecisionModeracionEntity.builder().build());
        when(notificacionMapper.toDocument(any(Notificacion.class)))
                .thenReturn(NotificacionDocument.builder().build());

        service.reactivarPerfil(cocineraId, "Cumplió castigo", adminId);

        assertFalse(perfil.isPausada());
        assertNotNull(perfil.getFechaReactivacion());
        verify(perfilRepository).save(perfil);
        verify(decisionRepository).save(any(DecisionModeracionEntity.class));
        verify(notificacionRepository).save(any(NotificacionDocument.class));
    }

    @Test
    @DisplayName("reactivarPerfil con perfil no pausado lanza ReglaDeNegocioException")
    void testReactivarPerfil_NoPausado() {
        PerfilCocineraEntity perfil = PerfilCocineraEntity.builder()
                .id(cocineraId)
                .pausada(false)
                .build();

        when(perfilRepository.findById(cocineraId)).thenReturn(Optional.of(perfil));

        assertThrows(ReglaDeNegocioException.class,
                () -> service.reactivarPerfil(cocineraId, "X", adminId));
        verify(decisionRepository, never()).save(any());
    }
}