package com.ollacercana.service;

import com.ollacercana.domain.Credenciales;
import com.ollacercana.domain.Cuenta;
import com.ollacercana.domain.EstadoCuenta;
import com.ollacercana.domain.Identidad;
import com.ollacercana.domain.Rol;
import com.ollacercana.exception.ConflictoException;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.mapper.CuentaEntityMapper;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.validator.ICuentaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaServiceImplTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private CuentaEntityMapper cuentaEntityMapper;

    @Mock
    private ICuentaValidator cuentaValidator;

    @InjectMocks
    private CuentaServiceImpl cuentaService;

    private Cuenta cuentaBase;

    @BeforeEach
    void setUp() {
        Identidad identidad = Identidad.builder()
                .nombre("Juan Perez")
                .correo("juan@gmail.com")
                .celular("3001234567")
                .build();

        Credenciales credenciales = Credenciales.builder()
                .contrasenaHash("Password123")
                .build();

        cuentaBase = Cuenta.builder()
                .id(1L)
                .identidad(identidad)
                .credenciales(credenciales)
                .roles(Set.of(Rol.COMPRADOR))
                .estado(EstadoCuenta.ACTIVO)
                .build();
    }

    @Test
    @DisplayName("1. Happy Path")
    void registrar_HappyPath_DebeRegistrarCuentaExitosamente() {
        // Arrange
        when(cuentaEntityMapper.toEntity(any(Cuenta.class))).thenReturn(cuentaBase);
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaBase);
        when(cuentaEntityMapper.toDomain(any(Cuenta.class))).thenReturn(cuentaBase);

        // Act
        Cuenta resultado = cuentaService.registrar(cuentaBase);

        // Assert
        assertNotNull(resultado);
        assertEquals("juan@gmail.com", resultado.getIdentidad().getCorreo());

        verify(cuentaValidator).validarCorreoUnico("juan@gmail.com");
        verify(cuentaValidator).validarCelularUnico("3001234567");
        verify(cuentaValidator).validarPasswordSegura("Password123");
        verify(cuentaRepository).save(any(Cuenta.class));
    }

    @Test
    @DisplayName("2. Correo duplicado (409)")
    void registrar_CorreoDuplicado_LanzaConflictoException() {
        // Arrange
        doThrow(new ConflictoException("El correo ya existe"))
                .when(cuentaValidator).validarCorreoUnico(anyString());

        // Act & Assert
        assertThrows(ConflictoException.class, () -> cuentaService.registrar(cuentaBase));

        verify(cuentaRepository, never()).save(any());
    }

    @Test
    @DisplayName("3. Celular duplicado (409)")
    void registrar_CelularDuplicado_LanzaConflictoException() {
        // Arrange
        doThrow(new ConflictoException("El celular ya existe"))
                .when(cuentaValidator).validarCelularUnico(anyString());

        // Act & Assert
        assertThrows(ConflictoException.class, () -> cuentaService.registrar(cuentaBase));

        verify(cuentaRepository, never()).save(any());
    }

    @Test
    @DisplayName("4. Contraseña inválida (422)")
    void registrar_PasswordInvalida_LanzaReglaDeNegocioException() {
        // Arrange
        doThrow(new ReglaDeNegocioException("Contraseña débil"))
                .when(cuentaValidator).validarPasswordSegura(anyString());

        // Act & Assert
        assertThrows(ReglaDeNegocioException.class, () -> cuentaService.registrar(cuentaBase));

        verify(cuentaRepository, never()).save(any());
    }

    @Test
    @DisplayName("5. Error inesperado (500)")
    void registrar_ErrorInesperado_LanzaRuntimeException() {
        // Arrange
        when(cuentaEntityMapper.toEntity(any(Cuenta.class)))
                .thenThrow(new RuntimeException("Error inesperado en la base de datos"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> cuentaService.registrar(cuentaBase));
    }
}