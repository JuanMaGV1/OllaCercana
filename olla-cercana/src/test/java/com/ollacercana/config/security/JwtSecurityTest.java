package com.ollacercana.config.security;

import com.ollacercana.config.security.CustomUserDetailsService;
import com.ollacercana.config.security.JwtAuthenticationFilter;
import com.ollacercana.config.security.JwtService;
import com.ollacercana.core.models.Cuenta;
import com.ollacercana.core.models.Identidad;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtSecurityTest {

    private static final String SECRET_TEST = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION_MS = 3600000; // 1 hora

    @Mock
    private PerfilCocineraRepository perfilCocineraRepository;

    @Mock
    private CustomUserDetailsService userDetailsService;

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtService = new JwtService(SECRET_TEST, EXPIRATION_MS, perfilCocineraRepository);
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("JwtService genera y valida tokens reales")
    void jwtService_pruebasGeneracionYValidacion() {
        // Arrange
        Cuenta cuenta = Cuenta.builder()
                .id(10L)
                .identidad(Identidad.builder().correo("test@ollacercana.com").build())
                .roles(Set.of(Rol.COCINERA))
                .build();
        when(perfilCocineraRepository.findByCuentaId(10L)).thenReturn(Optional.empty());

        // Act
        String token = jwtService.generarToken(cuenta);

        // Assert
        assertNotNull(token);
        assertTrue(jwtService.validarToken(token));
        assertEquals("10", jwtService.extraerSubject(token));
        assertEquals(10L, jwtService.extraerCuentaId(token));
        assertEquals("test@ollacercana.com", jwtService.extraerCorreo(token));
        assertTrue(jwtService.extraerRoles(token).contains("COCINERA"));
    }

    @Test
    @DisplayName("JwtAuthenticationFilter continúa la cadena si no hay header Authorization")
    void jwtAuthenticationFilter_sinHeader_continuaCadena() throws ServletException, IOException {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("JwtAuthenticationFilter autentica y continúa cuando el token Bearer es válido")
    void jwtAuthenticationFilter_conTokenValido_autenticaUsuario() throws ServletException, IOException {
        // Arrange
        Cuenta cuenta = Cuenta.builder()
                .id(5L)
                .identidad(Identidad.builder().correo("cocinera@ollacercana.com").build())
                .roles(Set.of(Rol.COCINERA))
                .build();
        when(perfilCocineraRepository.findByCuentaId(5L)).thenReturn(Optional.empty());
        String tokenReal = jwtService.generarToken(cuenta);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + tokenReal);

        UserDetails userDetails = User.builder()
                .username("cocinera@ollacercana.com")
                .password("hash123")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_COCINERA")))
                .build();
        when(userDetailsService.loadUserByUsername("cocinera@ollacercana.com")).thenReturn(userDetails);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("cocinera@ollacercana.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }
}