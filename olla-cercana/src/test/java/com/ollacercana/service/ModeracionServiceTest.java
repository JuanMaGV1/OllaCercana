package com.ollacercana.service;

import com.ollacercana.domain.*;
import com.ollacercana.dto.EjecutarDecisionDto;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.repository.*;
import com.ollacercana.repository.mongo.NotificacionRepository;
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
class ModeracionServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private DecisionModeracionRepository decisionRepository;

    @Mock
    private PlatoRepository platoRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private PerfilCocineraRepository perfilRepository;

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private ModeracionService moderacionService;

    private Reporte reportePlato;
    private Plato plato;
    private PerfilCocinera perfil;
    private final UUID reporteId = UUID.randomUUID();
    private final UUID platoId = UUID.randomUUID();
    private final UUID cocineraId = UUID.randomUUID();
    private final Long adminId = 99L;

    @BeforeEach
    void setUp() {
        reportePlato = Reporte.builder()
                .id(reporteId)
                .objetivo(ObjetivoReporte.PLATO)
                .objetivoId(platoId)
                .estado(EstadoReporte.ABIERTO)
                .build();

        plato = new Plato();
        plato.setId(platoId);
        plato.setCocineraId(cocineraId);
        plato.setEstado(EstadoPlato.ACTIVO);
        plato.setPorcionesTotales(5);
        plato.setPorcionesComprometidas(0);

        perfil = new PerfilCocinera();
        perfil.setId(cocineraId);
        perfil.setPausada(true);
    }

    @Test
    void testResolver_InhabilitarPublicacion() {
        when(reporteRepository.findById(reporteId)).thenReturn(Optional.of(reportePlato));
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        EjecutarDecisionDto dto = new EjecutarDecisionDto(TipoDecision.INHABILITAR_PUBLICACION, "Falta grave");
        moderacionService.resolver(reporteId, dto, adminId);

        assertEquals(EstadoReporte.RESUELTO, reportePlato.getEstado());
        assertEquals(EstadoPlato.OCULTO, plato.getEstado());

        verify(decisionRepository, times(1)).save(any(DecisionModeracion.class));
        verify(platoRepository, times(1)).save(plato);
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    void testResolver_RestaurarPublicacion() {
        reportePlato.setObjetivo(ObjetivoReporte.PLATO);
        plato.setEstado(EstadoPlato.OCULTO);
        
        when(reporteRepository.findById(reporteId)).thenReturn(Optional.of(reportePlato));
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        EjecutarDecisionDto dto = new EjecutarDecisionDto(TipoDecision.RESTAURAR_PUBLICACION, "Falsa alarma");
        moderacionService.resolver(reporteId, dto, adminId);

        assertEquals(EstadoReporte.RESUELTO, reportePlato.getEstado());
        assertEquals(EstadoPlato.ACTIVO, plato.getEstado());

        verify(decisionRepository, times(1)).save(any(DecisionModeracion.class));
        verify(platoRepository, times(1)).save(plato);
    }

    @Test
    void testResolver_YaResuelto() {
        reportePlato.setEstado(EstadoReporte.RESUELTO);
        when(reporteRepository.findById(reporteId)).thenReturn(Optional.of(reportePlato));

        EjecutarDecisionDto dto = new EjecutarDecisionDto(TipoDecision.DESCARTAR, "Nada");

        assertThrows(ReglaDeNegocioException.class, () -> moderacionService.resolver(reporteId, dto, adminId));
        verify(decisionRepository, never()).save(any());
    }

    @Test
    void testResolver_Descartar() {
        when(reporteRepository.findById(reporteId)).thenReturn(Optional.of(reportePlato));
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato)); // applyDecision will still be called and load plato

        EjecutarDecisionDto dto = new EjecutarDecisionDto(TipoDecision.DESCARTAR, "Falsa alarma");
        moderacionService.resolver(reporteId, dto, adminId);

        assertEquals(EstadoReporte.RESUELTO, reportePlato.getEstado());
        assertEquals(EstadoPlato.ACTIVO, plato.getEstado()); // Unchanged
        
        verify(decisionRepository, times(1)).save(any(DecisionModeracion.class));
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    void testResolver_Advertir() {
        when(reporteRepository.findById(reporteId)).thenReturn(Optional.of(reportePlato));
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato)); 

        EjecutarDecisionDto dto = new EjecutarDecisionDto(TipoDecision.ADVERTIR, "Cuidado con las porciones");
        moderacionService.resolver(reporteId, dto, adminId);

        assertEquals(EstadoReporte.RESUELTO, reportePlato.getEstado());
        assertEquals(EstadoPlato.ACTIVO, plato.getEstado()); // Unchanged

        verify(decisionRepository, times(1)).save(any(DecisionModeracion.class));
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    void testResolver_SuspenderCuenta() {
        Reporte reporteCuenta = Reporte.builder()
                .id(UUID.randomUUID())
                .objetivo(ObjetivoReporte.CUENTA)
                .objetivoId(cocineraId)
                .estado(EstadoReporte.ABIERTO)
                .build();
                
        Cuenta cuenta = new Cuenta();
        cuenta.setId(100L);
        cuenta.setEstado(EstadoCuenta.ACTIVO);
        perfil.setCuenta(cuenta);

        when(reporteRepository.findById(reporteCuenta.getId())).thenReturn(Optional.of(reporteCuenta));
        when(perfilRepository.findById(cocineraId)).thenReturn(Optional.of(perfil));

        EjecutarDecisionDto dto = new EjecutarDecisionDto(TipoDecision.SUSPENDER_CUENTA, "Violaciones repetidas");
        moderacionService.resolver(reporteCuenta.getId(), dto, adminId);

        assertEquals(EstadoReporte.RESUELTO, reporteCuenta.getEstado());
        assertEquals(EstadoCuenta.SUSPENDIDO, cuenta.getEstado());

        verify(cuentaRepository, times(1)).save(cuenta);
        verify(decisionRepository, times(1)).save(any(DecisionModeracion.class));
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    void testReactivarPerfil_Exito() {
        when(perfilRepository.findById(cocineraId)).thenReturn(Optional.of(perfil));

        moderacionService.reactivarPerfil(cocineraId, "Cumplió castigo", adminId);

        assertFalse(perfil.isPausada());
        assertNotNull(perfil.getFechaReactivacion());
        
        verify(perfilRepository, times(1)).save(perfil);
        verify(decisionRepository, times(1)).save(any(DecisionModeracion.class));
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    void testReactivarPerfil_NoPausado() {
        perfil.setPausada(false);
        when(perfilRepository.findById(cocineraId)).thenReturn(Optional.of(perfil));

        assertThrows(ReglaDeNegocioException.class, () -> moderacionService.reactivarPerfil(cocineraId, "X", adminId));
        verify(decisionRepository, never()).save(any());
    }
}
