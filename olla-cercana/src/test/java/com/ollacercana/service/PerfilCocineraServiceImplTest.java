package com.ollacercana.service;

import com.ollacercana.domain.*;
import com.ollacercana.dto.request.PerfilCocineraRequestDTO;
import com.ollacercana.dto.response.PerfilCocineraResponseDTO;
import com.ollacercana.exception.ConflictoException;
import com.ollacercana.mapper.PerfilCocineraMapper;
import com.ollacercana.repository.CodigoOTPRepository;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.service.impl.PerfilCocineraServiceImpl;
import com.ollacercana.validator.IPerfilCocineraValidator;
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
    private PerfilCocineraMapper perfilMapper;

    @Mock
    private IPerfilCocineraValidator validator;

    @InjectMocks
    private PerfilCocineraServiceImpl perfilService;

    private Cuenta cuenta;
    private PerfilCocinera perfil;
    private PerfilCocineraRequestDTO request;
    private PerfilCocineraResponseDTO responseDTO;
    private final UUID perfilId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        cuenta = Cuenta.builder()
                .id(1L)
                .roles(Set.of(Rol.COCINERA))
                .credenciales(Credenciales.builder().celularVerificado(false).build())
                .build();

        request = PerfilCocineraRequestDTO.builder()
                .cuentaId(1L)
                .presentacion("Comida típica casera")
                .conjuntoResidencial("Torres del Parque")
                .especialidades(List.of("Sancocho", "Bandeja Paisa"))
                .mediosPago(List.of(MedioPago.NEQUI))
                .numeroNequi("3001234567")
                .build();

        perfil = PerfilCocinera.builder()
                .id(perfilId)
                .presentacion("Comida típica casera")
                .conjuntoResidencial("Torres del Parque")
                .cuenta(cuenta)
                .verificada(false)
                .build();

        responseDTO = PerfilCocineraResponseDTO.builder()
                .id(perfilId)
                .cuentaId(1L)
                .presentacion("Comida típica casera")
                .conjuntoResidencial("Torres del Parque")
                .build();
    }

    @Test
    @DisplayName("Crear Perfil - Happy Path")
    void crearPerfil_Exitoso() {
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(perfilMapper.toDomain(request)).thenReturn(perfil);
        when(perfilRepository.save(any(PerfilCocinera.class))).thenReturn(perfil);
        when(perfilMapper.toResponseDTO(perfil)).thenReturn(responseDTO);

        PerfilCocineraResponseDTO result = perfilService.crearPerfil(request);

        assertNotNull(result);
        assertEquals("Torres del Parque", result.getConjuntoResidencial());
        verify(validator).validarParaCrear(request, cuenta);
        verify(perfilRepository).save(perfil);
    }

    @Test
    @DisplayName("Actualizar Perfil - Happy Path")
    void actualizarPerfil_Exitoso() {
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfil));
        when(perfilRepository.save(any(PerfilCocinera.class))).thenReturn(perfil);
        when(perfilMapper.toResponseDTO(perfil)).thenReturn(responseDTO);

        PerfilCocineraResponseDTO result = perfilService.actualizarPerfil(perfilId, request);

        assertNotNull(result);
        verify(validator).validarParaActualizar(perfilId, request);
        verify(perfilRepository).save(perfil);
    }

    @Test
    @DisplayName("Verificar Teléfono OTP - Exitoso")
    void verificarTelefono_OTPValido_RetornaTrueYVerifica() {
        CodigoOTP otp = CodigoOTP.builder()
                .perfilId(perfilId)
                .codigo("123456")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(5))
                .usado(false)
                .build();

        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfil));
        when(codigoOTPRepository.findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(perfilId))
                .thenReturn(Optional.of(otp));

        boolean resultado = perfilService.verificarTelefono(perfilId, "123456");

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
        CodigoOTP otp = CodigoOTP.builder()
                .perfilId(perfilId)
                .codigo("123456")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(5))
                .usado(false)
                .build();

        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfil));
        when(codigoOTPRepository.findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(perfilId))
                .thenReturn(Optional.of(otp));

        assertThrows(ConflictoException.class, () -> perfilService.verificarTelefono(perfilId, "000000"));
        assertFalse(perfil.isVerificada());
        verify(perfilRepository, never()).save(any());
    }
}