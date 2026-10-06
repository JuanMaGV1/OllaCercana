package com.ollacercana.service;

import com.ollacercana.domain.*;
import com.ollacercana.exception.ConflictoException;
import com.ollacercana.repository.CodigoOTPRepository;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.validator.IPerfilCocineraValidator;
import com.ollacercana.service.impl.PerfilCocineraServiceImpl;
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

    @Mock
    private PerfilCocineraRepository perfilRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private CodigoOTPRepository codigoOTPRepository;

    @Mock
    private IPerfilCocineraValidator validator;

    @InjectMocks
    private PerfilCocineraServiceImpl perfilService;

    private Cuenta cuenta;
    private PerfilCocinera perfil;
    private final UUID perfilId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        cuenta = Cuenta.builder()
                .id(1L)
                .roles(Set.of(Rol.COCINERA))
                .credenciales(Credenciales.builder().celularVerificado(false).build())
                .build();

        perfil = PerfilCocinera.builder()
                .id(perfilId)
                .presentacion("Comida típica casera")
                .conjuntoResidencial("Torres del Parque")
                .especialidades(List.of("Sancocho", "Bandeja Paisa"))
                .mediosPago(List.of(MedioPago.NEQUI))
                .numeroNequi("3001234567")
                .cuenta(cuenta)
                .verificada(false)
                .build();
    }

    @Test
    @DisplayName("Crear Perfil - Happy Path")
    void crearPerfil_Exitoso() {
        // Arrange
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(perfilRepository.save(any(PerfilCocinera.class))).thenReturn(perfil);

        // Act
        PerfilCocinera result = perfilService.crearPerfil(perfil, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Torres del Parque", result.getConjuntoResidencial());
        verify(validator).validarParaCrear(perfil, cuenta);
        verify(perfilRepository).save(perfil);
    }

    @Test
    @DisplayName("Actualizar Perfil - Happy Path")
    void actualizarPerfil_Exitoso() {
        // Arrange
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfil));
        when(perfilRepository.save(any(PerfilCocinera.class))).thenReturn(perfil);

        // Act
        PerfilCocinera result = perfilService.actualizarPerfil(perfilId, perfil);

        // Assert
        assertNotNull(result);
        verify(validator).validarParaActualizar(perfilId, perfil);
        verify(perfilRepository).save(perfil);
    }

    @Test
    @DisplayName("Verificar Teléfono OTP - Exitoso")
    void verificarTelefono_OTPValido_RetornaTrueYVerifica() {
        // Arrange
        CodigoOTP otp = CodigoOTP.builder()
                .perfilId(perfilId)
                .codigo("123456")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(5))
                .usado(false)
                .build();

        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfil));
        when(codigoOTPRepository.findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(perfilId))
                .thenReturn(Optional.of(otp));

        // Act
        boolean resultado = perfilService.verificarTelefono(perfilId, "123456");

        // Assert
        assertTrue(resultado);
        assertTrue(perfil.isVerificada());
        assertTrue(perfil.getCuenta().getCredenciales().getCelularVerificado());
        assertTrue(otp.isUsado());
        verify(codigoOTPRepository).save(otp);
        verify(perfilRepository).save(perfil);
    }

    @Test
    @DisplayName("Verificar Teléfono OTP - Código Erróneo Lanza ConflictoException")
    void verificarTelefono_OTPInvalido_LanzaConflictoException() {
        // Arrange
        CodigoOTP otp = CodigoOTP.builder()
                .perfilId(perfilId)
                .codigo("123456")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(5))
                .usado(false)
                .build();

        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfil));
        when(codigoOTPRepository.findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(perfilId))
                .thenReturn(Optional.of(otp));

        // Act & Assert
        assertThrows(ConflictoException.class, () -> perfilService.verificarTelefono(perfilId, "000000"));
        assertFalse(perfil.isVerificada());
        verify(perfilRepository, never()).save(any());
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

        assertThrows(com.ollacercana.exception.ResourceNotFoundException.class,
                () -> perfilService.actualizarPerfil(perfilId, perfil));
    }

    @Test
    @DisplayName("Verificar Teléfono OTP - Perfil no encontrado")
    void verificarTelefono_perfilNoExiste_lanzaNotFound() {
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.empty());

        assertThrows(com.ollacercana.exception.ResourceNotFoundException.class,
                () -> perfilService.verificarTelefono(perfilId, "123456"));
    }

    @Test
    @DisplayName("Verificar Teléfono OTP - No hay código OTP generado para el perfil")
    void verificarTelefono_sinOtpActivo_lanzaConflicto() {
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfil));
        when(codigoOTPRepository.findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(perfilId))
                .thenReturn(Optional.empty());

        assertThrows(ConflictoException.class, () -> perfilService.verificarTelefono(perfilId, "123456"));
    }

    @Test
    @DisplayName("Obtener por CuentaId - Retorna perfil cuando existe y lanza Conflicto cuando no")
    void obtenerPorCuentaId_comportamiento() {
        when(perfilRepository.findByCuentaId(1L)).thenReturn(Optional.of(perfil));
        assertEquals(perfil, perfilService.obtenerPorCuentaId(1L));

        when(perfilRepository.findByCuentaId(2L)).thenReturn(Optional.empty());
        assertThrows(ConflictoException.class, () -> perfilService.obtenerPorCuentaId(2L));
    }

    @Test
    @DisplayName("Listar destacadas - Llama al repositorio correspondiente")
    void listarDestacadas_retornaLista() {
        when(perfilRepository.findByEsDestacadaTrue()).thenReturn(List.of(perfil));
        List<PerfilCocinera> resultado = perfilService.listarDestacadas();
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("OC-253 / OC-256: Guardar y retornar métodos de pago configurados en el perfil")
    void perfil_guardaYRetornaMediosDePago() {
        PerfilCocinera perfilConMedios = PerfilCocinera.builder()
                .id(perfilId)
                .conjuntoResidencial("Torres del Parque")
                .mediosPago(List.of(MedioPago.NEQUI, MedioPago.DAVIPLATA, MedioPago.EFECTIVO))
                .numeroNequi("3001234567")
                .numeroDaviplata("3107654321")
                .cuenta(cuenta)
                .build();

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(perfilRepository.save(any(PerfilCocinera.class))).thenReturn(perfilConMedios);

        perfilService.crearPerfil(perfilConMedios, 1L);

        org.mockito.ArgumentCaptor<PerfilCocinera> captor = org.mockito.ArgumentCaptor.forClass(PerfilCocinera.class);
        verify(perfilRepository).save(captor.capture());

        PerfilCocinera persistido = captor.getValue();
        assertNotNull(persistido);
        assertNotNull(persistido.getMediosPago());
        assertEquals(3, persistido.getMediosPago().size());
        assertTrue(persistido.getMediosPago().contains(MedioPago.NEQUI));
        assertTrue(persistido.getMediosPago().contains(MedioPago.DAVIPLATA));
        assertTrue(persistido.getMediosPago().contains(MedioPago.EFECTIVO));
    }
}