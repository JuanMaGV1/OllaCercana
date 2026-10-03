package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.domain.*;
import com.ollacercana.dto.request.RegistroRequestDTO;
import com.ollacercana.exception.ConflictoException;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ICuentaService cuentaService;

    @Test
    @DisplayName("POST /api/v1/cuentas - 201 Created cuando los datos son válidos")
    void registrar_datosValidos_retorna201() throws Exception {
        RegistroRequestDTO request = RegistroRequestDTO.builder()
                .nombre("Laura Martínez")
                .correo("laura@ejemplo.com")
                .celular("3109876543")
                .contrasena("Segura1234")
                .rol(Rol.COCINERA)
                .build();

        Cuenta cuentaCreada = Cuenta.builder()
                .id(10L)
                .identidad(Identidad.builder()
                        .nombre(request.getNombre())
                        .correo(request.getCorreo())
                        .celular(request.getCelular())
                        .build())
                .roles(Set.of(Rol.COCINERA))
                .estado(EstadoCuenta.ACTIVO)
                .build();

        when(cuentaService.registrar(any(Cuenta.class))).thenReturn(cuentaCreada);

        mockMvc.perform(post("/api/v1/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.correo").value("laura@ejemplo.com"))
                .andExpect(jsonPath("$.rol").value("COCINERA"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    @DisplayName("POST /api/v1/cuentas - 400 Bad Request cuando el celular o correo tienen formato inválido")
    void registrar_formatoInvalido_retorna400() throws Exception {
        RegistroRequestDTO requestInvalido = RegistroRequestDTO.builder()
                .nombre("Laura")
                .correo("correo-invalido")
                .celular("12345") // Debe iniciar con 3 y tener 10 dígitos
                .contrasena("123") // Menor a 8 caracteres
                .build();

        mockMvc.perform(post("/api/v1/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.correo").exists())
                .andExpect(jsonPath("$.detalles.celular").exists())
                .andExpect(jsonPath("$.detalles.contrasena").exists());
    }

    @Test
    @DisplayName("POST /api/v1/cuentas - 409 Conflict cuando el correo ya existe")
    void registrar_correoDuplicado_retorna409() throws Exception {
        RegistroRequestDTO request = RegistroRequestDTO.builder()
                .nombre("Laura")
                .correo("duplicado@ejemplo.com")
                .celular("3109876543")
                .contrasena("Segura1234")
                .rol(Rol.COMPRADOR)
                .build();

        when(cuentaService.registrar(any(Cuenta.class)))
                .thenThrow(new ConflictoException("El correo ya se encuentra registrado"));

        mockMvc.perform(post("/api/v1/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El correo ya se encuentra registrado"));
    }
}