package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.domain.TipoComida;
import com.ollacercana.domain.RestriccionAlimentaria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PlatoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicar_debeRetornar201() throws Exception {
        PlatoRequestDTO request = new PlatoRequestDTO(
            "Arroz con pollo",
            "Arroz con pollo criollo con ensalada",
            "http://foto.com/arroz.jpg",
            TipoComida.ALMUERZO,
            List.of(RestriccionAlimentaria.SIN_GLUTEN),
            10,
            new BigDecimal("12000.00"),
            "Portería Torre 1"
        );

        mockMvc.perform(post("/api/v1/platos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.nombre").value("Arroz con pollo"))
            .andExpect(jsonPath("$.estado").value("ACTIVO"))
            .andExpect(jsonPath("$.porcionesDisponibles").value(10));
    }

    @Test
    void publicar_conDatosInvalidos_debeRetornar400() throws Exception {
        PlatoRequestDTO request = new PlatoRequestDTO(
            "ab",                    // muy corto
            "corta",                 // muy corta
            "",
            null,                    // nulo
            List.of(),
            0,                       // inválido
            new BigDecimal("100"),   // bajo el mínimo
            ""
        );

        mockMvc.perform(post("/api/v1/platos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}