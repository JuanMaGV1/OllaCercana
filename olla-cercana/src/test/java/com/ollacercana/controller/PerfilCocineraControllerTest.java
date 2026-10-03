package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.domain.MedioPago;
import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.dto.request.PerfilCocineraRequestDTO;
import com.ollacercana.dto.request.VerificarOtpRequestDTO;
import com.ollacercana.exception.ConflictoException;
import com.ollacercana.service.IPerfilCocineraService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PerfilCocineraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IPerfilCocineraService perfilService;

    private final UUID perfilId = UUID.randomUUID();

    @Test
    @DisplayName("POST /api/v1/perfiles - 201 Created")
    void crearPerfil_Exitoso() throws Exception {
        PerfilCocineraRequestDTO request = PerfilCocineraRequestDTO.builder()
                .cuentaId(1L)
                .presentacion("Especialista en comida típica")
                .conjuntoResidencial("Torres del Parque")
                .especialidades(List.of("Sancocho"))
                .mediosPago(List.of(MedioPago.NEQUI))
                .numeroNequi("3001234567")
                .build();

        PerfilCocinera perfilDominio = PerfilCocinera.builder()
                .id(perfilId)
                .presentacion("Especialista en comida típica")
                .conjuntoResidencial("Torres del Parque")
                .build();

        when(perfilService.crearPerfil(any(PerfilCocinera.class), eq(1L))).thenReturn(perfilDominio);

        mockMvc.perform(post("/api/v1/perfiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(perfilId.toString()))
                .andExpect(jsonPath("$.conjuntoResidencial").value("Torres del Parque"));
    }

    @Test
    @DisplayName("PUT /api/v1/perfiles/{id} - 200 OK")
    void actualizarPerfil_Exitoso() throws Exception {
        PerfilCocineraRequestDTO request = PerfilCocineraRequestDTO.builder()
                .cuentaId(1L)
                .presentacion("Presentación actualizada")
                .conjuntoResidencial("Torres del Parque")
                .mediosPago(List.of(MedioPago.DAVIPLATA))
                .numeroDaviplata("3001234567")
                .build();

        PerfilCocinera perfilDominio = PerfilCocinera.builder()
                .id(perfilId)
                .presentacion("Presentación actualizada")
                .conjuntoResidencial("Torres del Parque")
                .build();

        when(perfilService.actualizarPerfil(eq(perfilId), any(PerfilCocinera.class))).thenReturn(perfilDominio);

        mockMvc.perform(put("/api/v1/perfiles/" + perfilId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presentacion").value("Presentación actualizada"));
    }

    @Test
    @DisplayName("POST /api/v1/perfiles/{id}/verificar-telefono - 200 OK")
    void verificarTelefono_Exitoso() throws Exception {
        VerificarOtpRequestDTO request = VerificarOtpRequestDTO.builder()
                .codigo("123456")
                .build();

        when(perfilService.verificarTelefono(perfilId, "123456")).thenReturn(true);

        mockMvc.perform(post("/api/v1/perfiles/" + perfilId + "/verificar-telefono")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Teléfono verificado exitosamente"));
    }

    @Test
    @DisplayName("POST /api/v1/perfiles/{id}/verificar-telefono - 409 Conflict ante OTP inválido")
    void verificarTelefono_Invalido_Retorna409() throws Exception {
        VerificarOtpRequestDTO request = VerificarOtpRequestDTO.builder()
                .codigo("999999")
                .build();

        when(perfilService.verificarTelefono(perfilId, "999999"))
                .thenThrow(new ConflictoException("Código OTP inválido o expirado"));

        mockMvc.perform(post("/api/v1/perfiles/" + perfilId + "/verificar-telefono")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Código OTP inválido o expirado"));
    }
}