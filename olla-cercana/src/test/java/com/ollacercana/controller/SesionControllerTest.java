package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.exception.ConflictoException;
import com.ollacercana.model.domain.*;
import com.ollacercana.model.dto.request.LoginRequestDTO;
import com.ollacercana.security.JwtService;
import com.ollacercana.service.ICuentaService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SesionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ICuentaService cuentaService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("POST /api/v1/sesiones - 200 OK con credenciales válidas")
    void iniciarSesion_Exitoso_Retorna200YToken() throws Exception {
        Cuenta cuenta = Cuenta.builder()
                .id(1L)
                .identidad(Identidad.builder().nombre("Carlos Perez").correo("carlos@gmail.com").celular("3001234567").build())
                .roles(Set.of(Rol.COMPRADOR))
                .estado(EstadoCuenta.ACTIVA)
                .build();

        when(cuentaService.autenticar("carlos@gmail.com", "Password123")).thenReturn(cuenta);
        when(jwtService.generarToken(any(Cuenta.class))).thenReturn("jwt-token-valido");

        LoginRequestDTO request = LoginRequestDTO.builder()
                .identificador("carlos@gmail.com")
                .contrasena("Password123")
                .build();

        mockMvc.perform(post("/api/v1/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-valido"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Carlos Perez"))
                .andExpect(jsonPath("$.correo").value("carlos@gmail.com"))
                .andExpect(jsonPath("$.rol").value("COMPRADOR"));
    }

    @Test
    @DisplayName("POST /api/v1/sesiones - 409 Conflict ante credenciales erróneas")
    void iniciarSesion_CredencialesInvalidas_Retorna409() throws Exception {
        when(cuentaService.autenticar("carlos@gmail.com", "PasswordErroneo"))
                .thenThrow(new ConflictoException("Credenciales inválidas"));

        LoginRequestDTO request = LoginRequestDTO.builder()
                .identificador("carlos@gmail.com")
                .contrasena("PasswordErroneo")
                .build();

        mockMvc.perform(post("/api/v1/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    @DisplayName("POST /api/v1/sesiones - 400 Bad Request cuando faltan campos")
    void iniciarSesion_CamposVacios_Retorna400() throws Exception {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .identificador("")
                .contrasena("")
                .build();

        mockMvc.perform(post("/api/v1/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validación fallida"));
    }
}