package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.controller.dtos.request.PerfilCocineraRequestDTO;
import com.ollacercana.controller.dtos.request.VerificarOtpRequestDTO;
import com.ollacercana.controller.handlers.exception.ConflictoException;
import com.ollacercana.core.models.Cuenta;
import com.ollacercana.core.models.Identidad;
import com.ollacercana.core.models.PerfilCocinera;
import com.ollacercana.core.models.enums.MedioPago;
import com.ollacercana.core.services.IPerfilCocineraService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "1", roles = {"COCINERA", "ADMIN"})
class PerfilCocineraControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private IPerfilCocineraService perfilService;

    private final UUID perfilId = UUID.randomUUID();

    @Test
    @DisplayName("POST /api/v1/perfiles - 201 Created")
    void crearPerfil_Exitoso() throws Exception {
        PerfilCocineraRequestDTO request = PerfilCocineraRequestDTO.builder()
                .cuentaId(1L)
                .presentacion("Especialista en comida tÃ­pica")
                .conjuntoResidencial("Torres del Parque")
                .especialidades(List.of("Sancocho"))
                .mediosPago(List.of(MedioPago.NEQUI))
                .numeroNequi("3001234567")
                .build();

        // dominio puro â€” cuentaId se setea como campo independiente
        PerfilCocinera perfilDominio = PerfilCocinera.builder()
                .id(perfilId)
                .cuenta(com.ollacercana.core.models.Cuenta.builder().id(1L).build())
                .presentacion("Especialista en comida tÃ­pica")
                .conjuntoResidencial("Torres del Parque")
                .build();

        when(perfilService.crearPerfil(any(PerfilCocinera.class), eq(1L))).thenReturn(perfilDominio);

        mockMvc.perform(post("/api/v1/perfiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.conjuntoResidencial").value("Torres del Parque"));
    }

    @Test
    @DisplayName("PUT /api/v1/perfiles/{id} - 200 OK")
    void actualizarPerfil_Exitoso() throws Exception {
        PerfilCocineraRequestDTO request = PerfilCocineraRequestDTO.builder()
                .cuentaId(1L)
                .presentacion("PresentaciÃ³n actualizada")
                .conjuntoResidencial("Torres del Parque")
                .mediosPago(List.of(MedioPago.DAVIPLATA))
                .numeroDaviplata("3001234567")
                .build();

        PerfilCocinera perfilDominio = PerfilCocinera.builder()
                .id(perfilId)
                .cuenta(com.ollacercana.core.models.Cuenta.builder().id(1L).build())
                .presentacion("PresentaciÃ³n actualizada")
                .conjuntoResidencial("Torres del Parque")
                .build();

        when(perfilService.actualizarPerfil(eq(perfilId), any(PerfilCocinera.class))).thenReturn(perfilDominio);

        mockMvc.perform(put("/api/v1/perfiles/" + perfilId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presentacion").value("PresentaciÃ³n actualizada"));
    }

    @Test
    @DisplayName("POST /api/v1/perfiles/{id}/verificar-telefono - 200 OK")
    void verificarTelefono_Exitoso() throws Exception {
        VerificarOtpRequestDTO request = VerificarOtpRequestDTO.builder().codigo("123456").build();

        when(perfilService.verificarTelefono(perfilId, "123456")).thenReturn(true);

        mockMvc.perform(post("/api/v1/perfiles/" + perfilId + "/verificar-telefono")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tel\u00e9fono verificado exitosamente"));
    }

    @Test
    @DisplayName("POST /api/v1/perfiles/{id}/verificar-telefono - 409 Conflict ante OTP invÃ¡lido")
    void verificarTelefono_Invalido_Retorna409() throws Exception {
        VerificarOtpRequestDTO request = VerificarOtpRequestDTO.builder().codigo("999999").build();

        when(perfilService.verificarTelefono(perfilId, "999999"))
                .thenThrow(new ConflictoException("CÃ³digo OTP invÃ¡lido o expirado"));

        mockMvc.perform(post("/api/v1/perfiles/" + perfilId + "/verificar-telefono")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("CÃ³digo OTP invÃ¡lido o expirado"));
    }

    @Test
        @DisplayName("GET /api/v1/perfiles/cuenta/{cuentaId} - 200 OK")
        void obtenerPorCuentaId_Exitoso() throws Exception {
        Cuenta cuenta = Cuenta.builder()
                .id(1L)
                .identidad(new Identidad("Maria", "maria@test.com", "3001234567", null))
                .build();

        PerfilCocinera perfil = PerfilCocinera.builder()
                .id(UUID.randomUUID())
                .cuenta(cuenta)
                .conjuntoResidencial("Torres del Parque")
                .build();

        when(perfilService.obtenerPorCuentaId(1L)).thenReturn(perfil);

        mockMvc.perform(get("/api/v1/perfiles/cuenta/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.conjuntoResidencial").value("Torres del Parque"))
                .andExpect(jsonPath("$.nombreCocinera").value("Maria"));
        }

    @Test
    @DisplayName("GET /api/v1/perfiles/destacadas - 200 OK")
    void listarDestacadas_Exitoso() throws Exception {
        PerfilCocinera destacada = PerfilCocinera.builder()
                .id(perfilId)
                .conjuntoResidencial("Bosques Verdes")
                .esDestacada(true)
                .build();

        when(perfilService.listarDestacadas()).thenReturn(List.of(destacada));

        mockMvc.perform(get("/api/v1/perfiles/destacadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].conjuntoResidencial").value("Bosques Verdes"));
    }
}