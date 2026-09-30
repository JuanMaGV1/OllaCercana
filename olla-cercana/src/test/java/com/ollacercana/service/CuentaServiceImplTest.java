package com.ollacercana.service;

import com.ollacercana.exception.ConflictoException;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.mapper.CuentaEntityMapper;
import com.ollacercana.model.domain.Credenciales;
import com.ollacercana.model.domain.Cuenta;
import com.ollacercana.model.domain.EstadoCuenta;
import com.ollacercana.model.domain.Identidad;
import com.ollacercana.model.domain.Rol;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.service.impl.CuentaServiceImpl;
import com.ollacercana.validator.ICuentaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
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
    private final String rawPassword = "Password123";
    private final String bCryptHash = new BCryptPasswordEncoder().encode(rawPassword);

    @BeforeEach
    void setUp() {
        Identidad identidad = Identidad.builder()
                .nombre("Juan Perez")
                .correo("juan@gmail.com")
                .celular("3001234567")
                .build();

        Credenciales credenciales = Credenciales.builder()
                .contrasenaHash(rawPassword)
                .build();

        cuentaBase = Cuenta.builder()
                .id(1L)
                .identidad(identidad)
                .credenciales(credenciales)
                .roles(Set.of(Rol.COMPRADOR))
                .estado(EstadoCuenta.ACTIVO)
                .build();
    }

    @Nested
    @DisplayName("Pruebas de Registro")
    class RegistroTests {

        @Test
        @DisplayName("1. Registro exitoso (Happy Path)")
        void registrar_HappyPath_DebeRegistrarCuentaExitosamente() {
            when(cuentaEntityMapper.toEntity(any(Cuenta.class))).thenReturn(cuentaBase);
            when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaBase);
            when(cuentaEntityMapper.toDomain(any(Cuenta.class))).thenReturn(cuentaBase);

            Cuenta resultado = cuentaService.registrar(cuentaBase);

            assertNotNull(resultado);
            assertEquals("juan@gmail.com", resultado.getIdentidad().getCorreo());
            verify(cuentaValidator).validarCorreoUnico("juan@gmail.com");
            verify(cuentaValidator).validarCelularUnico("3001234567");
            verify(cuentaValidator).validarPasswordSegura(anyString());
            verify(cuentaRepository).save(any(Cuenta.class));
        }

        @Test
        @DisplayName("2. Correo duplicado (409)")
        void registrar_CorreoDuplicado_LanzaConflictoException() {
            doThrow(new ConflictoException("El correo ya existe"))
                    .when(cuentaValidator).validarCorreoUnico(anyString());

            assertThrows(ConflictoException.class, () -> cuentaService.registrar(cuentaBase));
            verify(cuentaRepository, never()).save(any());
        }

        @Test
        @DisplayName("3. Celular duplicado (409)")
        void registrar_CelularDuplicado_LanzaConflictoException() {
            doThrow(new ConflictoException("El celular ya existe"))
                    .when(cuentaValidator).validarCelularUnico(anyString());

            assertThrows(ConflictoException.class, () -> cuentaService.registrar(cuentaBase));
            verify(cuentaRepository, never()).save(any());
        }

        @Test
        @DisplayName("4. Contraseña inválida (422)")
        void registrar_PasswordInvalida_LanzaReglaDeNegocioException() {
            doThrow(new ReglaDeNegocioException("Contraseña débil"))
                    .when(cuentaValidator).validarPasswordSegura(anyString());

            assertThrows(ReglaDeNegocioException.class, () -> cuentaService.registrar(cuentaBase));
            verify(cuentaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Pruebas de Autenticación")
    class AutenticarTests {

        @Test
        @DisplayName("1. Autenticación exitosa con correo o celular (Happy Path)")
        void autenticar_CredencialesValidas_RetornaCuenta() {
            // Arrange: Cuenta con hash real de BCrypt
            cuentaBase.getCredenciales().setContrasenaHash(bCryptHash);
            when(cuentaRepository.findByIdentificador("juan@gmail.com"))
                    .thenReturn(Optional.of(cuentaBase));

            // Act
            Cuenta resultado = cuentaService.autenticar("juan@gmail.com", rawPassword);

            // Assert
            assertNotNull(resultado);
            assertEquals("juan@gmail.com", resultado.getIdentidad().getCorreo());
            assertEquals(EstadoCuenta.ACTIVO, resultado.getEstado());
        }

        @Test
        @DisplayName("2. Contraseña incorrecta (409 ConflictoException)")
        void autenticar_PasswordIncorrecta_LanzaConflictoException() {
            // Arrange
            cuentaBase.getCredenciales().setContrasenaHash(bCryptHash);
            when(cuentaRepository.findByIdentificador("juan@gmail.com"))
                    .thenReturn(Optional.of(cuentaBase));

            // Act & Assert
            ConflictoException exception = assertThrows(
                    ConflictoException.class,
                    () -> cuentaService.autenticar("juan@gmail.com", "PasswordErronea123")
            );

            assertEquals("Credenciales inválidas", exception.getMessage());
        }

        @Test
        @DisplayName("3. Identificador inexistente (409 ConflictoException)")
        void autenticar_IdentificadorNoExiste_LanzaConflictoException() {
            // Arrange
            when(cuentaRepository.findByIdentificador("noexiste@gmail.com"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ConflictoException exception = assertThrows(
                    ConflictoException.class,
                    () -> cuentaService.autenticar("noexiste@gmail.com", rawPassword)
            );

            assertEquals("Credenciales inválidas", exception.getMessage());
        }

        @Test
        @DisplayName("4. Cuenta bloqueada (409 ConflictoException)")
        void autenticar_CuentaBloqueada_LanzaConflictoException() {
            // Arrange
            cuentaBase.setEstado(EstadoCuenta.BLOQUEADO);
            cuentaBase.getCredenciales().setContrasenaHash(bCryptHash);
            when(cuentaRepository.findByIdentificador("juan@gmail.com"))
                    .thenReturn(Optional.of(cuentaBase));

            // Act & Assert
            ConflictoException exception = assertThrows(
                    ConflictoException.class,
                    () -> cuentaService.autenticar("juan@gmail.com", rawPassword)
            );

            assertEquals("La cuenta se encuentra bloqueada", exception.getMessage());
        }
    }
}