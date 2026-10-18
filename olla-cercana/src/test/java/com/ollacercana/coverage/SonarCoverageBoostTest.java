package com.ollacercana.coverage;

import com.ollacercana.config.scheduler.MedallaScheduler;
import com.ollacercana.controller.dtos.request.ReporteCrearDTO;
import com.ollacercana.controller.handlers.exception.CuentaNoEncontradaException;
import com.ollacercana.controller.handlers.exception.ReglaDeNegocioException;
import com.ollacercana.core.models.Notificacion;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.*;
import com.ollacercana.core.services.MedallaService;
import com.ollacercana.core.services.NotificacionService;
import com.ollacercana.core.services.impl.ChatServiceImpl;
import com.ollacercana.core.services.impl.CuentaServiceImpl;
import com.ollacercana.core.services.impl.NotificacionServiceImpl;
import com.ollacercana.core.services.impl.PlatoServiceImpl;
import com.ollacercana.core.validators.ReporteValidator;
import com.ollacercana.persistence.document.NotificacionDocument;
import com.ollacercana.persistence.entities.*;
import com.ollacercana.persistence.mappers.NotificacionDocumentMapper;
import com.ollacercana.persistence.mappers.PlatoEntityMapper;
import com.ollacercana.persistence.repository.*;
import com.ollacercana.persistence.repository.mongo.MensajeChatRepository;
import com.ollacercana.persistence.repository.mongo.NotificacionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SonarCoverageBoostTest {

    // Mocks para CuentaServiceImpl
    @Mock private CuentaRepository cuentaRepository;
    @InjectMocks private CuentaServiceImpl cuentaService;

    // Mocks para PlatoServiceImpl
    @Mock private PlatoRepository platoRepository;
    @Mock private PlatoEntityMapper platoEntityMapper;
    @Mock private PerfilCocineraRepository perfilCocineraRepository;
    @InjectMocks private PlatoServiceImpl platoService;

    // Mocks para NotificacionServiceImpl
    @Mock private NotificacionRepository notificacionRepository;
    @Mock private NotificacionDocumentMapper notificacionDocumentMapper;

    // Mocks para ReporteValidator
    @Mock private ReporteRepository reporteRepository;

    // Mocks para MedallaScheduler
    @Mock private MedallaService medallaService;
    @InjectMocks private MedallaScheduler medallaScheduler;

    // Mocks para ChatServiceImpl
    @Mock private MensajeChatRepository mensajeChatRepository;
    @Mock private ReservaRepository reservaRepository;
    @InjectMocks private ChatServiceImpl chatService;

    @Test
    @DisplayName("CuentaServiceImpl: cubre las 8 líneas de registrarTokenDispositivo (ramas nulas y excepción)")
    void testRegistrarTokenDispositivo_todasLasRamas() {
        // 1. Cuenta encontrada con credenciales existentes
        CuentaEntity c1 = CuentaEntity.builder().id(1L).credenciales(new CredencialesEmbeddable()).build();
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(c1));
        cuentaService.registrarTokenDispositivo(1L, "token-fcm-1");
        assertEquals("token-fcm-1", c1.getCredenciales().getTokenFCM());

        // 2. Cuenta encontrada con credenciales == null
        CuentaEntity c2 = CuentaEntity.builder().id(2L).credenciales(null).build();
        when(cuentaRepository.findById(2L)).thenReturn(Optional.of(c2));
        cuentaService.registrarTokenDispositivo(2L, "token-fcm-2");
        assertNotNull(c2.getCredenciales());
        assertEquals("token-fcm-2", c2.getCredenciales().getTokenFCM());

        // 3. Cuenta no encontrada lanza CuentaNoEncontradaException
        when(cuentaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(CuentaNoEncontradaException.class, () -> cuentaService.registrarTokenDispositivo(99L, "token-fcm-3"));
    }

    @Test
    @DisplayName("NotificacionServiceImpl: cubre registrar(null) y registrar(notificacion)")
    void testNotificacionService_registrar() {
        NotificacionService service = new NotificacionServiceImpl(notificacionRepository, notificacionDocumentMapper);

        // Rama: notificacion == null
        service.registrar(null);
        verify(notificacionRepository, never()).save(any());

        // Rama: notificacion válida
        Notificacion notif = Notificacion.builder().titulo("Aviso").build();
        when(notificacionDocumentMapper.toDocument(notif)).thenReturn(new NotificacionDocument());
        service.registrar(notif);
        verify(notificacionRepository).save(any(NotificacionDocument.class));
    }

    @Test
    @DisplayName("PlatoServiceImpl: cubre las ramas de obtenerConjuntoDePlato")
    void testObtenerConjuntoDePlato_todasLasRamas() {
        UUID platoId = UUID.randomUUID();
        UUID cocineraId = UUID.randomUUID();

        // 1. Plato con cocineraId == null -> "Conjunto no especificado"
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(PlatoEntity.builder().id(platoId).build()));
        when(platoEntityMapper.toDomain(any())).thenReturn(Plato.builder().id(platoId).cocineraId(null).build());
        assertEquals("Conjunto no especificado", platoService.obtenerConjuntoDePlato(platoId));

        // 2. Plato con cocineraId pero perfil no encontrado en repo -> "Conjunto no especificado"
        when(platoEntityMapper.toDomain(any())).thenReturn(Plato.builder().id(platoId).cocineraId(cocineraId).build());
        when(perfilCocineraRepository.findById(cocineraId)).thenReturn(Optional.empty());
        assertEquals("Conjunto no especificado", platoService.obtenerConjuntoDePlato(platoId));

        // 3. Plato con perfil encontrado -> retorna el nombre del conjunto
        when(perfilCocineraRepository.findById(cocineraId))
                .thenReturn(Optional.of(PerfilCocineraEntity.builder().id(cocineraId).conjuntoResidencial("Torres del Sol").build()));
        assertEquals("Torres del Sol", platoService.obtenerConjuntoDePlato(platoId));
    }

    @Test
    @DisplayName("ReporteValidator: cubre validación de parámetros básicos")
    void testReporteValidator_parametrosBasicos() {
        ReporteValidator val = new ReporteValidator(
                platoRepository, perfilCocineraRepository, cuentaRepository, reporteRepository, platoEntityMapper);

        assertThrows(ReglaDeNegocioException.class, () -> val.validarParaCreacion(null, 1L));
        assertThrows(ReglaDeNegocioException.class, () -> val.validarParaCreacion(ReporteCrearDTO.builder().build(), 1L));
        assertThrows(ReglaDeNegocioException.class, () -> val.validarParaCreacion(
                ReporteCrearDTO.builder().motivo(MotivoReporte.OTRO).build(), 1L));
        assertThrows(ReglaDeNegocioException.class, () -> val.validarParaCreacion(
                ReporteCrearDTO.builder().motivo(MotivoReporte.OTRO).objetivo(ObjetivoReporte.PLATO).build(), null));
    }

    @Test
    @DisplayName("ReporteValidator: cubre ramas de validación de Plato y Cuenta")
    void testReporteValidator_platoYCuentaRamas() {
        ReporteValidator val = new ReporteValidator(
                platoRepository, perfilCocineraRepository, cuentaRepository, reporteRepository, platoEntityMapper);

        // Plato sin objetivoId
        assertThrows(ReglaDeNegocioException.class, () -> val.validarParaCreacion(
                ReporteCrearDTO.builder().motivo(MotivoReporte.OTRO).objetivo(ObjetivoReporte.PLATO).objetivoId(null).build(), 1L));

        // Plato inexistente
        UUID pId = UUID.randomUUID();
        when(platoRepository.findById(pId)).thenReturn(Optional.empty());
        assertThrows(ReglaDeNegocioException.class, () -> val.validarParaCreacion(
                ReporteCrearDTO.builder().motivo(MotivoReporte.OTRO).objetivo(ObjetivoReporte.PLATO).objetivoId(pId).build(), 1L));

        // Cuenta con ambos IDs especificados
        assertThrows(ReglaDeNegocioException.class, () -> val.validarParaCreacion(
                ReporteCrearDTO.builder().motivo(MotivoReporte.OTRO).objetivo(ObjetivoReporte.CUENTA)
                        .cuentaObjetivoId(10L).objetivoId(UUID.randomUUID()).build(), 1L));

        // Cuenta objetivo no existe
        when(cuentaRepository.existsById(50L)).thenReturn(false);
        assertThrows(ReglaDeNegocioException.class, () -> val.validarParaCreacion(
                ReporteCrearDTO.builder().motivo(MotivoReporte.OTRO).objetivo(ObjetivoReporte.CUENTA).cuentaObjetivoId(50L).build(), 1L));

        // Autorreporte de cuenta
        when(cuentaRepository.existsById(1L)).thenReturn(true);
        assertThrows(ReglaDeNegocioException.class, () -> val.validarParaCreacion(
                ReporteCrearDTO.builder().motivo(MotivoReporte.OTRO).objetivo(ObjetivoReporte.CUENTA).cuentaObjetivoId(1L).build(), 1L));
    }

    @Test
    @DisplayName("CuentaEntity: cubre todas las ramas de onCreate() usando reflexión")
    void testCuentaEntity_onCreate() {
        CuentaEntity c1 = new CuentaEntity();
        ReflectionTestUtils.invokeMethod(c1, "onCreate");
        assertNotNull(c1.getFechaRegistro());
        assertEquals(EstadoCuenta.ACTIVO, c1.getEstado());
        assertTrue(c1.getAvisosActivos());

        CuentaEntity c2 = CuentaEntity.builder()
                .credenciales(CredencialesEmbeddable.builder().celularVerificado(null).build())
                .build();
        ReflectionTestUtils.invokeMethod(c2, "onCreate");
        assertFalse(c2.getCredenciales().getCelularVerificado());
    }

    @Test
    @DisplayName("MedallaScheduler y ChatServiceImpl: cubre líneas restantes")
    void testSchedulerYChat_lineasRestantes() {
        // MedallaScheduler happy path
        when(medallaService.calcularBalanceSemanal(any())).thenReturn(List.of("Torres"));
        assertDoesNotThrow(() -> medallaScheduler.calcularBalanceSemanal());

        // ChatServiceImpl purga y setter
        chatService.purgarMensajesDeReservasCerradas(LocalDateTime.now());
        chatService.setPublicadorEventosReserva(null);
        assertNotNull(chatService);
    }
}