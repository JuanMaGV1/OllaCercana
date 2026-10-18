package com.ollacercana.config.security;

import com.ollacercana.core.models.Cuenta;
import com.ollacercana.core.models.Identidad;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.persistence.repository.CuentaRepository;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityBranchesTest {

    @Mock private PerfilCocineraRepository perfilRepo;
    @Mock private CuentaRepository cuentaRepo;
    @Mock private CustomUserDetailsService userDetailsService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("JwtService: cubre branches de extracción de claims y salvedades de firma")
    void jwtService_branches() {
        // 1. Clave que no es Base64 (salta al catch de getSigningKey)
        JwtService rawKeyService = new JwtService("mi-clave-secreta-no-base64-muy-larga-1234567890", 3600000L, perfilRepo);
        Cuenta cuenta = Cuenta.builder().id(99L).identidad(Identidad.builder().correo("a@b.com").build()).roles(Set.of(Rol.ADMIN)).build();
        String token = rawKeyService.generarToken(cuenta);
        assertNotNull(token);

        // 2. Token con cuentaId null y subject no numérico
        Cuenta cuentaSinId = Cuenta.builder().identidad(Identidad.builder().correo("test@mail.com").build()).build();
        String tokenSinId = rawKeyService.generarToken(cuentaSinId);
        assertNull(rawKeyService.extraerCuentaId(tokenSinId));

        // 3. Extracción de roles vacíos con token válido y cocineraId nulo
        Cuenta cuentaSinRoles = Cuenta.builder().id(2L).roles(null).build();
        String tokenSinRoles = rawKeyService.generarToken(cuentaSinRoles);
        assertTrue(rawKeyService.extraerRoles(tokenSinRoles).isEmpty());
        assertNull(rawKeyService.extraerCocineraId(tokenSinId));

        // 4. Token inválido retorna false en validarToken
        assertFalse(rawKeyService.validarToken("token-invalido"));
    }

    @Test
    @DisplayName("JwtAuthenticationFilter: branches de prefijo no Bearer y roles alternativos")
    void filter_branches() throws ServletException, IOException {
        JwtService jwtService = new JwtService("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970", 3600000L, perfilRepo);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        // 1. Header con formato distinto a Bearer
        when(request.getHeader("Authorization")).thenReturn("Basic 12345");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);

        // 2. Token válido donde userDetails no tiene authorities previas
        Cuenta c = Cuenta.builder().id(1L).identidad(Identidad.builder().correo("user@test.com").build()).roles(Set.of(Rol.COMPRADOR)).build();
        String token = jwtService.generarToken(c);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        UserDetails userSinAuth = new User("user@test.com", "pass", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userSinAuth);

        filter.doFilter(request, response, chain);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("UsuarioActual: branches de permisos y errores")
    void usuarioActual_branches() {
        UsuarioActual usuarioActual = new UsuarioActual(cuentaRepo, perfilRepo);

        // 1. Sin autenticación
        assertTrue(usuarioActual.getCocineraIdOpt().isEmpty());

        // 2. Con autenticación sin roles
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("1", null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertTrue(usuarioActual.getRoles().isEmpty());
    }
}