package com.ollacercana.controller;

import com.ollacercana.controller.dtos.request.MapaCocinerasRequestDTO;
import com.ollacercana.controller.dtos.response.CocineraMapaResponseDTO;
import com.ollacercana.core.services.CocineraMapaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CocineraMapaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CocineraMapaService cocineraMapaService;

    @Test
    @DisplayName("OC-233 / OC-236: 200 OK público con cocineras en el área")
    void consultarMapa_exitoso_retorna200() throws Exception {
        UUID cocineraId = UUID.randomUUID();
        CocineraMapaResponseDTO dto = CocineraMapaResponseDTO.builder()
                .cocineraId(cocineraId)
                .nombreCocinera("Doña Rosa")
                .latitudOfuscada(4.6800)
                .longitudOfuscada(-74.0550)
                .fotoPlato("https://fotos.com/plato.jpg")
                .nombrePlato("Sancocho")
                .precio(new BigDecimal("15000"))
                .distanciaMetros(300)
                .build();

        when(cocineraMapaService.buscarCocinerasEnMapa(any(MapaCocinerasRequestDTO.class)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/cocineras/mapa")
                        .param("latitud", "4.6789")
                        .param("longitud", "-74.0567")
                        .param("radio", "2000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cocineraId").value(cocineraId.toString()))
                .andExpect(jsonPath("$[0].nombreCocinera").value("Doña Rosa"))
                .andExpect(jsonPath("$[0].latitudOfuscada").value(4.6800))
                .andExpect(jsonPath("$[0].longitudOfuscada").value(-74.0550))
                .andExpect(jsonPath("$[0].distanciaMetros").value(300))
                .andExpect(jsonPath("$[0].precio").value(15000));
    }

    @Test
    @DisplayName("OC-233 / OC-236: 200 OK con lista vacía cuando no hay cocineras en la zona")
    void consultarMapa_sinCocineras_retorna200ListaVacia() throws Exception {
        when(cocineraMapaService.buscarCocinerasEnMapa(any(MapaCocinerasRequestDTO.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/cocineras/mapa")
                        .param("latitud", "4.6789")
                        .param("longitud", "-74.0567")
                        .param("radio", "500")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("OC-231 / OC-236: 400 Bad Request cuando la latitud está fuera de rango [-90, 90]")
    void consultarMapa_latitudInvalida_retorna400() throws Exception {
        mockMvc.perform(get("/api/v1/cocineras/mapa")
                        .param("latitud", "95.0")
                        .param("longitud", "-74.0567")
                        .param("radio", "2000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("OC-231 / OC-236: 400 Bad Request cuando la longitud está fuera de rango [-180, 180]")
    void consultarMapa_longitudInvalida_retorna400() throws Exception {
        mockMvc.perform(get("/api/v1/cocineras/mapa")
                        .param("latitud", "4.6789")
                        .param("longitud", "200.0")
                        .param("radio", "2000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("OC-231 / OC-236: 400 Bad Request cuando el radio es negativo o cero")
    void consultarMapa_radioInvalido_retorna400() throws Exception {
        mockMvc.perform(get("/api/v1/cocineras/mapa")
                        .param("latitud", "4.6789")
                        .param("longitud", "-74.0567")
                        .param("radio", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("OC-231 / OC-236: 400 Bad Request cuando el radio supera el máximo configurable")
    void consultarMapa_radioExcedeMaximo_retorna400() throws Exception {
        mockMvc.perform(get("/api/v1/cocineras/mapa")
                        .param("latitud", "4.6789")
                        .param("longitud", "-74.0567")
                        .param("radio", "60000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("OC-231 / OC-236: 400 Bad Request cuando faltan parámetros obligatorios")
    void consultarMapa_parametrosFaltantes_retorna400() throws Exception {
        mockMvc.perform(get("/api/v1/cocineras/mapa")
                        .param("latitud", "4.6789")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}