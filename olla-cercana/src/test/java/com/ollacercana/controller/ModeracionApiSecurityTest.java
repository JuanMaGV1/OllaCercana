package com.ollacercana.controller;

import com.ollacercana.core.services.ModeracionService;
import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.config.security.JwtService;
import com.ollacercana.config.security.CustomUserDetailsService;
import com.ollacercana.config.security.CustomAccessDeniedHandler;
import com.ollacercana.config.security.CustomAuthenticationEntryPoint;
import com.ollacercana.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;

@WebMvcTest(ModeracionApi.class)
@Import({SecurityConfig.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
class ModeracionApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModeracionService moderacionService;

    @MockBean
    private UsuarioActual usuarioActual;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

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