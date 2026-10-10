package com.ollacercana.controller;

import com.ollacercana.core.services.IModeracionService;
import com.ollacercana.config.security.UsuarioActual;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;

/**
 * HU-19 / OC-035: solo ADMIN puede acceder al panel de moderación.
 * Escenario 3 de HU-19: "No tiene permisos para ver esta sección".
 *
 * Se usa @SpringBootTest para que la cadena de seguridad completa (incluido
 * el AuthorizationManagerBeforeMethodInterceptor de @EnableMethodSecurity)
 * esté activa. Con @WebMvcTest el interceptor de @PreAuthorize no se aplica
 * y los tests de rol dan falsos positivos.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ModeracionApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IModeracionService moderacionService;

    @MockBean
    private UsuarioActual usuarioActual;

    @Test
    @WithMockUser(roles = "COMPRADOR")
    void testAccesoDenegadoParaComprador() throws Exception {
        mockMvc.perform(get("/api/v1/admin/moderacion/reportes"))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("$.message", is("No tiene permisos para ver esta sección")));
    }

    @Test
    @WithMockUser(roles = "COCINERA")
    void testAccesoDenegadoParaCocinera() throws Exception {
        mockMvc.perform(get("/api/v1/admin/moderacion/reportes"))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("$.message", is("No tiene permisos para ver esta sección")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAccesoPermitidoParaAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/moderacion/reportes"))
               .andExpect(status().isOk());
    }
}