package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.dtos.request.TokenDispositivoRequestDTO;
import com.ollacercana.core.services.ICuentaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DispositivoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ICuentaService cuentaService;
    @MockBean private UsuarioActual usuarioActual;

    @Test
    @WithMockUser(username = "1", roles = "COMPRADOR")
    @DisplayName("POST /api/v1/dispositivos/token - 200 OK")
    void registrarToken_Exitoso() throws Exception {
        when(usuarioActual.getCuentaId()).thenReturn(1L);

        TokenDispositivoRequestDTO dto = new TokenDispositivoRequestDTO("fcm_token_12345");

        mockMvc.perform(post("/api/v1/dispositivos/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Token del dispositivo registrado exitosamente"));

        verify(cuentaService).registrarTokenDispositivo(1L, "fcm_token_12345");
    }

    @Test
    @WithMockUser(username = "1", roles = "COMPRADOR")
    @DisplayName("POST /api/v1/dispositivos/token - 400 Bad Request cuando el token es blanco")
    void registrarToken_Invalido() throws Exception {
        TokenDispositivoRequestDTO dto = new TokenDispositivoRequestDTO("   ");

        mockMvc.perform(post("/api/v1/dispositivos/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}