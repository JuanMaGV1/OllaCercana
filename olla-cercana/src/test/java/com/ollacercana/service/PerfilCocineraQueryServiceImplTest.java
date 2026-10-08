package com.ollacercana.service;

import com.ollacercana.controller.handlers.exception.ConflictoException;
import com.ollacercana.controller.handlers.exception.ResourceNotFoundException;
import com.ollacercana.core.models.Credenciales;
import com.ollacercana.core.models.Cuenta;
import com.ollacercana.core.models.PerfilCocinera;
import com.ollacercana.core.models.enums.MedioPago;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.services.impl.PerfilCocineraServiceImpl;
import com.ollacercana.core.validators.IPerfilCocineraValidator;
import com.ollacercana.persistence.entities.CodigoOTPEntity;
import com.ollacercana.persistence.entities.CuentaEntity;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.mappers.CuentaEntityMapper;
import com.ollacercana.persistence.mappers.PerfilCocineraDomainMapper;
import com.ollacercana.persistence.mappers.PerfilCocineraPersistenceMapper;
import com.ollacercana.persistence.repository.CodigoOTPRepository;
import com.ollacercana.persistence.repository.CuentaRepository;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilCocineraServiceImplTest {

    @Mock private PerfilCocineraRepository perfilRepository;
    @Mock private CuentaRepository cuentaRepository;
    @Mock private CodigoOTPRepository codigoOTPRepository;
    @Mock private IPerfilCocineraValidator validator;
    @Mock private PerfilCocineraDomainMapper domainMapper;
    @Mock private PerfilCocineraPersistenceMapper persistenceMapper;
    @Mock private CuentaEntityMapper cuentaMapper;

    @InjectMocks private PerfilCocineraServiceImpl perfilService;

    private Cuenta cuenta;
    private CuentaEntity cuentaEntity;
    private PerfilCocinera perfil;
    private final UUID perfilId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        cuenta = Cuenta.builder()
                .id(1L)
                .roles(Set.of(Rol.COCINERA))
                .credenciales(Credenciales.builder().celularVerificado(false).build())
                .build();

        cuentaEntity = CuentaEntity.builder()
                .id(1L)
                .roles(Set.of(Rol.COCINERA))
                .build();

        // ✅ dominio puro: solo cuentaId (no `cuenta` anidada)
        perfil = PerfilCocinera.builder()
                .id(perfilId)
                .cuenta(cuenta)
                .presentacion("Comida típica casera")
                .conjuntoResidencial("Torres del Parque")
                .especialidades(List.of("Sancocho", "Bandeja Paisa"))
                .mediosPago(List.of(MedioPago.NEQUI))
                .numeroNequi("3001234567")
                .verificada(false)
                .build();

        // Mappers bidireccionales
        lenient().when(cuentaMapper.toDomain(any(CuentaEntity.class))).thenReturn(cuenta);
        lenient().when(persistenceMapper.toEntity(any(PerfilCocinera.class))).thenAnswer(i -> {
            PerfilCocinera p = i.getArgument(0);
            return PerfilCocineraEntity.builder()
                    .id(p.getId())
                    .presentacion(p.getPresentacion())
                    .conjuntoResidencial(p.getConjuntoResidencial())
                    .especialidades(p.getEspecialidades())
                    .mediosPago(p.getMediosPago())
                    .numeroNequi(p.getNumeroNequi())
                    .numeroDaviplata(p.getNumeroDaviplata())
                    .verificada(p.isVerificada())
                    .pausada(p.isPausada())
                    .build();
        });
        lenient().when(domainMapper.toDomain(any(PerfilCocineraEntity.class))).thenAnswer(i -> {
            PerfilCocineraEntity e = i.getArgument(0);
            return PerfilCocinera.builder()
                    .id(e.getId())
                    .presentacion(e.getPresentacion())
                    .conjuntoResidencial(e.getConjuntoResidencial())
                    .especialidades(e.getEspecialidades())
                    .mediosPago(e.getMediosPago())
                    .numeroNequi(e.getNumeroNequi())
                    .numeroDaviplata(e.getNumeroDaviplata())
                    .verificada(e.isVerificada())
                    .pausada(e.isPausada())
                    .cuenta(e.getCuenta() != null
        ? Cuenta.builder().id(e.getCuenta().getId()).build()
        : null)
                    .build();
        });
    }

    @Test
    @DisplayName("Crear Perfil - Happy Path")
    void crearPerfil_Exitoso() {
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaEntity));

        PerfilCocineraEntity entidadGuardada = PerfilCocineraEntity.builder()
                .id(perfilId)
                .conjuntoResidencial("Torres del Parque")
                .cuenta(cuentaEntity)
                .build();
        when(perfilRepository.save(any(PerfilCocineraEntity.class))).thenReturn(entidadGuardada);

        PerfilCocinera result = perfilService.crearPerfil(perfil, 1L);

        assertNotNull(result);
        assertEquals("Torres del Parque", result.getConjuntoResidencial());
        verify(validator).validarParaCrear(perfil, cuenta);
        verify(perfilRepository).save(any(PerfilCocineraEntity.class));
    }

    @Test
    @DisplayName("Actualizar Perfil - Happy Path")
    void actualizarPerfil_Exitoso() {
        PerfilCocineraEntity existente = PerfilCocineraEntity.builder()
                .id(perfilId)
                .conjuntoResidencial("Viejo")
                .build();
        PerfilCocineraEntity actualizado = PerfilCocineraEntity.builder()
                .id(perfilId)
                .conjuntoResidencial("Torres del Parque")
                .build();

        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(existente));
        when(perfilRepository.save(any(PerfilCocineraEntity.class))).thenReturn(actualizado);

        PerfilCocinera result = perfilService.actualizarPerfil(perfilId, perfil);

        assertNotNull(result);
        verify(validator).validarParaActualizar(perfilId, perfil);
        verify(perfilRepository).save(existente);
    }

    @Test
    @DisplayName("Verificar Teléfono OTP - Exitoso")
    void verificarTelefono_OTPValido_RetornaTrueYVerifica() {
        PerfilCocineraEntity perfilEntity = PerfilCocineraEntity.builder()
                .id(perfilId)
                .cuenta(cuentaEntity)
                .build();

        CodigoOTPEntity otp = CodigoOTPEntity.builder()
                .perfilId(perfilId)
                .codigo("123456")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(5))
                .usado(false)
                .build();

        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfilEntity));
        when(codigoOTPRepository.findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(perfilId))
                .thenReturn(Optional.of(otp));

        boolean resultado = perfilService.verificarTelefono(perfilId, "123456");

        assertTrue(resultado);
        assertTrue(perfilEntity.isVerificada());
        assertTrue(otp.isUsado());
        verify(codigoOTPRepository).save(otp);
        verify(perfilRepository).save(perfilEntity);
    }

    @Test
    @DisplayName("Verificar Teléfono OTP - Código Erróneo Lanza ConflictoException")
    void verificarTelefono_OTPInvalido_LanzaConflictoException() {
        PerfilCocineraEntity perfilEntity = PerfilCocineraEntity.builder()
                .id(perfilId)
                .cuenta(cuentaEntity)
                .build();

        CodigoOTPEntity otp = CodigoOTPEntity.builder()
                .perfilId(perfilId)
                .codigo("123456")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(5))
                .usado(false)
                .build();

        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfilEntity));
        when(codigoOTPRepository.findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(perfilId))
                .thenReturn(Optional.of(otp));

        assertThrows(ConflictoException.class,
                () -> perfilService.verificarTelefono(perfilId, "000000"));
        assertFalse(perfilEntity.isVerificada());
    }

    @Test
    @DisplayName("Crear Perfil - Cuenta no existe lanza ConflictoException")
    void crearPerfil_cuentaNoExiste_lanzaConflicto() {
        when(cuentaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ConflictoException.class, () -> perfilService.crearPerfil(perfil, 99L));
        verify(perfilRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar Perfil - Perfil inexistente lanza ResourceNotFoundException")
    void actualizarPerfil_noExiste_lanzaNotFound() {
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> perfilService.actualizarPerfil(perfilId, perfil));
    }

    @Test
    @DisplayName("Verificar Teléfono OTP - Perfil no encontrado")
    void verificarTelefono_perfilNoExiste_lanzaNotFound() {
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> perfilService.verificarTelefono(perfilId, "123456"));
    }

    @Test
    @DisplayName("Verificar Teléfono OTP - No hay código OTP generado para el perfil")
    void verificarTelefono_sinOtpActivo_lanzaConflicto() {
        PerfilCocineraEntity perfilEntity = PerfilCocineraEntity.builder().id(perfilId).build();
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfilEntity));
        when(codigoOTPRepository.findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(perfilId))
                .thenReturn(Optional.empty());

        assertThrows(ConflictoException.class,
                () -> perfilService.verificarTelefono(perfilId, "123456"));
    }

    @Test
    @DisplayName("Obtener por CuentaId - Retorna perfil cuando existe y lanza Conflicto cuando no")
    void obtenerPorCuentaId_comportamiento() {
        PerfilCocineraEntity entity = PerfilCocineraEntity.builder()
                .id(perfilId)
                .conjuntoResidencial("Torres del Parque")
                .build();
        when(perfilRepository.findByCuentaId(1L)).thenReturn(Optional.of(entity));

        PerfilCocinera resultado = perfilService.obtenerPorCuentaId(1L);
        assertEquals(perfilId, resultado.getId());

        when(perfilRepository.findByCuentaId(2L)).thenReturn(Optional.empty());
        assertThrows(ConflictoException.class, () -> perfilService.obtenerPorCuentaId(2L));
    }

    @Test
    @DisplayName("Listar destacadas - Llama al repositorio correspondiente")
    void listarDestacadas_retornaLista() {
        PerfilCocineraEntity entity = PerfilCocineraEntity.builder().id(perfilId).esDestacada(true).build();
        when(perfilRepository.findByEsDestacadaTrue()).thenReturn(List.of(entity));

        List<PerfilCocinera> resultado = perfilService.listarDestacadas();
        assertEquals(1, resultado.size());
    }
}