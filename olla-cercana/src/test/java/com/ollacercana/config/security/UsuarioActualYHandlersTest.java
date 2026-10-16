package com.ollacercana.config.security;

import com.ollacercana.controller.handlers.exception.AccesoDenegadoException;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.persistence.entities.CuentaEntity;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.repository.CuentaRepository;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioActualYHandlersTest {

    @Mock private CuentaRepository cuentaRepository;
    @Mock private PerfilCocineraRepository perfilCocineraRepository;

    @InjectMocks private UsuarioActual usuarioActual;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ──────────────── UsuarioActual ────────────────

    @Test
    @DisplayName("UsuarioActual: sin autenticación lanza AccesoDenegadoException")
    void usuarioActual_sinAutenticacion_lanzaExcepcion() {
        SecurityContextHolder.clearContext();
        assertThrows(AccesoDenegadoException.class, () -> usuarioActual.getCuentaId());
        assertTrue(usuarioActual.getCocineraIdOpt().isEmpty());
    }

    @Test
    @DisplayName("UsuarioActual: con CuentaUserDetails resuelve cuentaId y cocineraId")
    void usuarioActual_conCuentaUserDetails() {
        UUID cocineraId = UUID.randomUUID();
        CuentaUserDetails details = new CuentaUserDetails(
                10L, "correo@test.com", "pass", cocineraId, true,
                List.of(new SimpleGrantedAuthority("ROLE_COCINERA"))
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals(10L, usuarioActual.getCuentaId());
        assertEquals(cocineraId, usuarioActual.getCocineraId());
        assertEquals("correo@test.com", usuarioActual.getIdentificador());
        assertTrue(usuarioActual.tieneRol(Rol.COCINERA));
        assertFalse(usuarioActual.tieneRol(Rol.ADMIN));
    }

    @Test
    @DisplayName("UsuarioActual: con username numérico resuelve cuentaId y busca perfil")
    void usuarioActual_conUsernameNumerico() {
        UUID cocineraId = UUID.randomUUID();
        Authentication auth = new UsernamePasswordAuthenticationToken("25", null, List.of(new SimpleGrantedAuthority("ROLE_COMPRADOR")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(perfilCocineraRepository.findByCuentaId(25L))
                .thenReturn(Optional.of(PerfilCocineraEntity.builder().id(cocineraId).build()));

        assertEquals(25L, usuarioActual.getCuentaId());
        assertEquals(cocineraId, usuarioActual.getCocineraId());
    }

    @Test
    @DisplayName("UsuarioActual: con correo resuelve cuentaId vía repositorio")
    void usuarioActual_conCorreoResuelveViaRepo() {
        Authentication auth = new UsernamePasswordAuthenticationToken("juan@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        CuentaEntity entity = CuentaEntity.builder().id(77L).build();
        when(cuentaRepository.findByIdentificador("juan@test.com")).thenReturn(Optional.of(entity));

        assertEquals(77L, usuarioActual.getCuentaId());
    }

    @Test
    @DisplayName("UsuarioActual: cocineraId inexistente lanza excepción en getCocineraId")
    void usuarioActual_sinPerfilCocinera_lanzaExcepcion() {
        Authentication auth = new UsernamePasswordAuthenticationToken("10", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(perfilCocineraRepository.findByCuentaId(10L)).thenReturn(Optional.empty());

        assertThrows(AccesoDenegadoException.class, () -> usuarioActual.getCocineraId());
    }

    // ──────────────── Handlers de Seguridad ────────────────

    @Test
    @DisplayName("CustomAccessDeniedHandler: ruta admin muestra mensaje específico de admin")
    void accessDeniedHandler_rutaAdmin() throws IOException {
        CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/moderacion");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Forbidden"));

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("No tiene permisos para ver esta sección"));
    }

    @Test
    @DisplayName("CustomAccessDeniedHandler: ruta normal muestra mensaje genérico")
    void accessDeniedHandler_rutaNormal() throws IOException {
        CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/platos");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Forbidden"));

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("No tienes permisos suficientes para realizar esta acción"));
    }

    @Test
    @DisplayName("CustomAuthenticationEntryPoint: responde 401 Unauthorized")
    void authenticationEntryPoint_responde401() throws IOException {
        CustomAuthenticationEntryPoint entryPoint = new CustomAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/platos");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, null);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("No estás autenticado"));
    }
}