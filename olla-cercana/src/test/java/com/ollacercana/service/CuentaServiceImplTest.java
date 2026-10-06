package com.ollacercana.service;

import com.ollacercana.exception.ConflictoException;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.mapper.CuentaEntityMapper;
import com.ollacercana.model.domain.*;
import com.ollacercana.persistence.entity.CredencialesEmbeddable;
import com.ollacercana.persistence.entity.CuentaEntity;
import com.ollacercana.persistence.entity.IdentidadEmbeddable;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaServiceImplTest {

    @Mock private CuentaRepository cuentaRepository;
    @Mock private ICuentaValidator cuentaValidator;
    @Mock private CuentaEntityMapper entityMapper;
    @Spy  private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @InjectMocks private CuentaServiceImpl cuentaService;

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

        // ✅ Mapper bidireccional (dominio <-> entity)
        lenient().when(entityMapper.toEntity(any(Cuenta.class))).thenAnswer(i -> {
            Cuenta c = i.getArgument(0);
            return CuentaEntity.builder()
                    .id(c.getId())
                    .identidad(c.getIdentidad() != null ? IdentidadEmbeddable.builder()
                            .nombre(c.getIdentidad().getNombre())
                            .correo(c.getIdentidad().getCorreo())
                            .celular(c.getIdentidad().getCelular())
                            .build() : null)
                    .credenciales(c.getCredenciales() != null ? CredencialesEmbeddable.builder()
                            .contrasenaHash(c.getCredenciales().getContrasenaHash())
                            .celularVerificado(c.getCredenciales().getCelularVerificado())
                            .build() : null)
                    .roles(c.getRoles())
                    .estado(c.getEstado())
                    .fechaRegistro(c.getFechaRegistro())
                    .build();
        });

        lenient().when(entityMapper.toDomain(any(CuentaEntity.class))).thenAnswer(i -> {
            CuentaEntity e = i.getArgument(0);
            return Cuenta.builder()
                    .id(e.getId())
                    .identidad(e.getIdentidad() != null ? Identidad.builder()
                            .nombre(e.getIdentidad().getNombre())
                            .correo(e.getIdentidad().getCorreo())
                            .celular(e.getIdentidad().getCelular())
                            .build() : null)
                    .credenciales(e.getCredenciales() != null ? Credenciales.builder()
                            .contrasenaHash(e.getCredenciales().getContrasenaHash())
                            .celularVerificado(e.getCredenciales().getCelularVerificado())
                            .build() : null)
                    .roles(e.getRoles())
                    .estado(e.getEstado())
                    .fechaRegistro(e.getFechaRegistro())
                    .build();
        });
    }

    @Nested
    @DisplayName("Pruebas de Registro")
    class RegistroTests {

        @Test
        @DisplayName("1. Registro exitoso (Happy Path)")
        void registrar_HappyPath_DebeRegistrarCuentaExitosamente() {
            CuentaEntity entidadGuardada = CuentaEntity.builder().id(1L).build();
            when(cuentaRepository.save(any(CuentaEntity.class))).thenReturn(entidadGuardada);

            Cuenta resultado = cuentaService.registrar(cuentaBase);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
            verify(cuentaValidator).validarCorreoUnico("juan@gmail.com");
            verify(cuentaValidator).validarCelularUnico("3001234567");
            verify(cuentaValidator).validarPasswordSegura(anyString());
            verify(cuentaRepository).save(any(CuentaEntity.class));
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
            cuentaBase.getCredenciales().setContrasenaHash(bCryptHash);
            CuentaEntity entidad = CuentaEntity.builder()
                    .id(1L)
                    .identidad(IdentidadEmbeddable.builder()
                            .correo("juan@gmail.com").celular("3001234567").build())
                    .credenciales(CredencialesEmbeddable.builder()
                            .contrasenaHash(bCryptHash).build())
                    .roles(Set.of(Rol.COMPRADOR))
                    .estado(EstadoCuenta.ACTIVO)
                    .build();

            when(cuentaRepository.findByIdentificador("juan@gmail.com"))
                    .thenReturn(Optional.of(entidad));

            Cuenta resultado = cuentaService.autenticar("juan@gmail.com", rawPassword);

            assertNotNull(resultado);
            assertEquals("juan@gmail.com", resultado.getIdentidad().getCorreo());
            assertEquals(EstadoCuenta.ACTIVO, resultado.getEstado());
        }

        @Test
        @DisplayName("2. Contraseña incorrecta (401 BadCredentialsException)")
        void autenticar_PasswordIncorrecta_LanzaBadCredentialsException() {
            CuentaEntity entidad = CuentaEntity.builder()
                    .id(1L)
                    .identidad(IdentidadEmbeddable.builder().correo("juan@gmail.com").build())
                    .credenciales(CredencialesEmbeddable.builder().contrasenaHash(bCryptHash).build())
                    .roles(Set.of(Rol.COMPRADOR))
                    .estado(EstadoCuenta.ACTIVO)
                    .build();
            when(cuentaRepository.findByIdentificador("juan@gmail.com")).thenReturn(Optional.of(entidad));

            BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                    () -> cuentaService.autenticar("juan@gmail.com", "PasswordErronea123"));

            assertEquals("Credenciales inválidas", ex.getMessage());
        }

        @Test
        @DisplayName("3. Identificador inexistente (401 BadCredentialsException)")
        void autenticar_IdentificadorNoExiste_LanzaBadCredentialsException() {
            when(cuentaRepository.findByIdentificador("noexiste@gmail.com")).thenReturn(Optional.empty());

            BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                    () -> cuentaService.autenticar("noexiste@gmail.com", rawPassword));

            assertEquals("Credenciales inválidas", ex.getMessage());
        }

        @Test
        @DisplayName("4. Cuenta bloqueada (LockedException)")
        void autenticar_CuentaBloqueada_LanzaLockedException() {
            CuentaEntity entidad = CuentaEntity.builder()
                    .id(1L)
                    .identidad(IdentidadEmbeddable.builder().correo("juan@gmail.com").build())
                    .credenciales(CredencialesEmbeddable.builder().contrasenaHash(bCryptHash).build())
                    .roles(Set.of(Rol.COMPRADOR))
                    .estado(EstadoCuenta.BLOQUEADO)
                    .build();
            when(cuentaRepository.findByIdentificador("juan@gmail.com")).thenReturn(Optional.of(entidad));

            LockedException ex = assertThrows(LockedException.class,
                    () -> cuentaService.autenticar("juan@gmail.com", rawPassword));

            assertEquals("La cuenta se encuentra bloqueada", ex.getMessage());
        }
    }
}