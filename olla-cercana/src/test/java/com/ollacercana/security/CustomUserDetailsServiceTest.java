package com.ollacercana.security;

import com.ollacercana.model.domain.EstadoCuenta;
import com.ollacercana.model.domain.Rol;
import com.ollacercana.persistence.entity.CredencialesEmbeddable;
import com.ollacercana.persistence.entity.CuentaEntity;
import com.ollacercana.persistence.entity.IdentidadEmbeddable;
import com.ollacercana.persistence.entity.PerfilCocineraEntity;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.repository.PerfilCocineraRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private CuentaRepository cuentaRepository;
    @Mock private PerfilCocineraRepository perfilCocineraRepository;

    @InjectMocks private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("loadUserByUsername - Cuenta activa existente carga UserDetails con ROLE_*")
    void loadUserByUsername_usuarioExiste_retornaUserDetailsActivo() {
        UUID cocineraId = UUID.randomUUID();

        CuentaEntity cuenta = CuentaEntity.builder()
                .id(1L)
                .identidad(IdentidadEmbeddable.builder()
                        .correo("maria@ollacercana.com")
                        .build())
                .credenciales(CredencialesEmbeddable.builder()
                        .contrasenaHash("$2a$10$dummyhash")
                        .celularVerificado(true)
                        .build())
                .roles(Set.of(Rol.COCINERA))
                .estado(EstadoCuenta.ACTIVO)
                .build();

        when(cuentaRepository.findByIdentificador("maria@ollacercana.com")).thenReturn(Optional.of(cuenta));
        when(perfilCocineraRepository.findByCuentaId(1L))
                .thenReturn(Optional.of(PerfilCocineraEntity.builder().id(cocineraId).build()));

        UserDetails userDetails = userDetailsService.loadUserByUsername("maria@ollacercana.com");

        assertNotNull(userDetails);
        assertEquals("maria@ollacercana.com", userDetails.getUsername());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_COCINERA")));
    }

    @Test
    @DisplayName("loadUserByUsername - Cuenta no encontrada lanza UsernameNotFoundException")
    void loadUserByUsername_usuarioNoExiste_lanzaExcepcion() {
        when(cuentaRepository.findByIdentificador("desconocido@ollacercana.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("desconocido@ollacercana.com"));
    }

    @Test
    @DisplayName("loadUserByUsername - Cuenta bloqueada queda deshabilitada")
    void loadUserByUsername_cuentaBloqueada_retornaUserDetailsDeshabilitado() {
        CuentaEntity cuentaBloqueada = CuentaEntity.builder()
                .id(2L)
                .identidad(IdentidadEmbeddable.builder()
                        .correo("bloqueado@ollacercana.com")
                        .build())
                .credenciales(CredencialesEmbeddable.builder()
                        .contrasenaHash("$2a$10$dummyhash")
                        .build())
                .roles(Set.of(Rol.COMPRADOR))
                .estado(EstadoCuenta.BLOQUEADO)
                .build();

        when(cuentaRepository.findByIdentificador("bloqueado@ollacercana.com")).thenReturn(Optional.of(cuentaBloqueada));
        when(perfilCocineraRepository.findByCuentaId(2L)).thenReturn(Optional.empty());

        UserDetails userDetails = userDetailsService.loadUserByUsername("bloqueado@ollacercana.com");

        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
        assertFalse(userDetails.isAccountNonLocked());
    }
}