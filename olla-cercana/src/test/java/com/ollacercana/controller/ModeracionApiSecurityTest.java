package com.ollacercana.controller;

import com.ollacercana.service.ModeracionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModeracionApi.class)
class ModeracionApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModeracionService moderacionService;

    // We can test that a non-admin role gets a 403 Forbidden
    // The exact message is returned by the CustomAccessDeniedHandler which might not be loaded in a simple WebMvcTest 
    // unless explicitly imported, but we can verify the 403 status.
    @Test
    @WithMockUser(roles = "COMPRADOR")
    void testAccesoDenegadoParaComprador() throws Exception {
        mockMvc.perform(get("/api/v1/admin/moderacion/reportes"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COCINERA")
    void testAccesoDenegadoParaCocinera() throws Exception {
        mockMvc.perform(get("/api/v1/admin/moderacion/reportes"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAccesoPermitidoParaAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/moderacion/reportes"))
               .andExpect(status().isOk());
    }
}
