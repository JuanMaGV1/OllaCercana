package com.ollacercana.security;

import com.ollacercana.domain.Cuenta;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class JwtSecurityTest {

    @Test
    @DisplayName("JwtService genera y valida tokens mock")
    void jwtService_pruebas() {
        JwtService jwtService = new JwtService();
        String token = jwtService.generarToken(new Cuenta());

        assertNotNull(token);
        assertTrue(jwtService.validarToken(token));
    }

    @Test
    @DisplayName("JwtAuthenticationFilter continúa la cadena de filtros")
    void jwtAuthenticationFilter_continuaCadena() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}