package com.ollacercana.service;

import com.ollacercana.exception.ConflictoException;
import com.ollacercana.mapper.CuentaEntityMapper;
import com.ollacercana.mapper.PerfilCocineraEntityMapper;
import com.ollacercana.model.domain.*;
import com.ollacercana.model.dto.request.PerfilCocineraRequestDTO;
import com.ollacercana.persistence.entity.CodigoOTPEntity;
import com.ollacercana.persistence.entity.CuentaEntity;
import com.ollacercana.persistence.entity.PerfilCocineraEntity;
import com.ollacercana.repository.CodigoOTPRepository;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.service.impl.PerfilCocineraServiceImpl;
import com.ollacercana.validator.IPerfilCocineraValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilCocineraServiceImplTest {

    @Mock private PerfilCocineraRepository perfilRepository;
    @Mock private CuentaRepository cuentaRepository;
    @Mock private CodigoOTPRepository codigoOTPRepository;
    @Mock private PerfilCocineraEntityMapper perfilMapper;
    @Mock private CuentaEntityMapper cuentaMapper;
    @Mock private IPerfilCocineraValidator validator;

    @InjectMocks
    private PerfilCocineraServiceImpl service;

    private UUID cuentaId;
    private CuentaEntity cuentaEntity;
    private Cuenta cuentaDomain;

    @BeforeEach
    void setUp() {
        cuentaId = UUID.randomUUID();

        cuentaEntity = CuentaEntity.builder()
                .id(cuentaId)
                .correo("test@test.com")
                .celular("3001234567")
                .nombre("Test")
                .estado(EstadoCuenta.ACTIVA)
                .roles(java.util.Set.of(Rol.COCINERA))
                .build();

        cuentaDomain = Cuenta.builder()
                .id(cuentaId)
                .identidad(Identidad.builder().nombre("Test").correo("test@test.com").build())
                .credenciales(Credenciales.builder().celularVerificado(true).build())
                .estado(EstadoCuenta.ACTIVA)
                .roles(java.util.Set.of(Rol.COCINERA))
                .build();
    }

    @Test
    void crearPerfil_debePersistirYGenerarOtp() {
        PerfilCocineraRequestDTO request = new PerfilCocineraRequestDTO();
        request.setCuentaId(cuentaId);
        request.setPresentacion("Comida casera");
        request.setConjuntoResidencial("Torres del Parque");
        request.setEspecialidades(List.of("Almuerzos"));
        request.setMediosPago(List.of(MedioPago.NEQUI));
        request.setNumeroNequi("3001234567");

        PerfilCocineraEntity entityGuardada = PerfilCocineraEntity.builder()
                .id(UUID.randomUUID())
                .cuentaId(cuentaId)
                .presentacion("Comida casera")
                .conjuntoResidencial("Torres del Parque")
                .verificada(false)
                .pausada(false)
                .build();

        when(cuentaRepository.findById(cuentaId)).thenReturn(Optional.of(cuentaEntity));
        when(cuentaMapper.toDomain(cuentaEntity)).thenReturn(cuentaDomain);
        when(perfilMapper.toEntity(any(PerfilCocinera.class))).thenReturn(entityGuardada);
        when(perfilRepository.save(any(PerfilCocineraEntity.class))).thenReturn(entityGuardada);
        when(perfilMapper.toDomain(entityGuardada)).thenReturn(
                PerfilCocinera.builder()
                        .id(entityGuardada.getId())
                        .cuentaId(cuentaId)
                        .presentacion("Comida casera")
                        .conjuntoResidencial("Torres del Parque")
                        .verificada(false)
                        .pausada(false)
                        .build()
        );
        when(codigoOTPRepository.save(any(CodigoOTPEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var response = service.crearPerfil(request);

        assertNotNull(response);
        assertEquals(cuentaId, response.getCuentaId());
        verify(validator).validarParaCrear(eq(request), eq(cuentaDomain));
        verify(perfilRepository).save(any(PerfilCocineraEntity.class));

        ArgumentCaptor<CodigoOTPEntity> otpCaptor = ArgumentCaptor.forClass(CodigoOTPEntity.class);
        verify(codigoOTPRepository).save(otpCaptor.capture());
        assertNotNull(otpCaptor.getValue().getCodigo());
        assertEquals(6, otpCaptor.getValue().getCodigo().length());
    }

    @Test
    void crearPerfil_cuentaNoExiste_debeLanzarConflicto() {
        PerfilCocineraRequestDTO request = new PerfilCocineraRequestDTO();
        request.setCuentaId(cuentaId);

        when(cuentaRepository.findById(cuentaId)).thenReturn(Optional.empty());

        assertThrows(ConflictoException.class, () -> service.crearPerfil(request));
        verifyNoInteractions(perfilRepository);
    }

    @Test
    void verificarTelefono_otpValido_debeActivarPerfilYCuenta() {
        UUID perfilId = UUID.randomUUID();
        PerfilCocineraEntity perfilEntity = PerfilCocineraEntity.builder()
                .id(perfilId)
                .cuentaId(cuentaId)
                .verificada(false)
                .pausada(false)
                .build();

        CodigoOTPEntity otpEntity = CodigoOTPEntity.builder()
                .id(UUID.randomUUID())
                .perfilId(perfilId)
                .codigo("123456")
                .fechaExpiracion(java.time.LocalDateTime.now().plusMinutes(5))
                .usado(false)
                .build();

        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfilEntity));
        when(codigoOTPRepository.findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(perfilId))
                .thenReturn(Optional.of(otpEntity));
        when(perfilMapper.toDomain(perfilEntity)).thenReturn(
                PerfilCocinera.builder().id(perfilId).cuentaId(cuentaId).verificada(false).pausada(false).build()
        );
        when(cuentaRepository.findById(cuentaId)).thenReturn(Optional.of(cuentaEntity));
        when(cuentaMapper.toDomain(cuentaEntity)).thenReturn(cuentaDomain);

        boolean resultado = service.verificarTelefono(perfilId, "123456");

        assertTrue(resultado);
        assertTrue(otpEntity.isUsado());
        verify(perfilRepository).save(any(PerfilCocineraEntity.class));
        verify(cuentaRepository).save(any(CuentaEntity.class));
    }

    @Test
    void verificarTelefono_otpInvalido_debeLanzarConflicto() {
        UUID perfilId = UUID.randomUUID();
        PerfilCocineraEntity perfilEntity = PerfilCocineraEntity.builder()
                .id(perfilId).cuentaId(cuentaId).build();

        CodigoOTPEntity otpEntity = CodigoOTPEntity.builder()
                .perfilId(perfilId)
                .codigo("999999")
                .fechaExpiracion(java.time.LocalDateTime.now().plusMinutes(5))
                .usado(false)
                .build();

        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfilEntity));
        when(codigoOTPRepository.findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(perfilId))
                .thenReturn(Optional.of(otpEntity));

        assertThrows(ConflictoException.class, () -> service.verificarTelefono(perfilId, "000000"));
        verify(perfilRepository, never()).save(any());
    }
}