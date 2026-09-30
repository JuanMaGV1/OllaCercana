package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.domain.TipoComida;
import com.ollacercana.domain.RestriccionAlimentaria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PlatoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void asegurarCocineraSembrada() {
        UUID cocineraId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Integer existe = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM perfiles_cocinera WHERE id = ?", Integer.class, cocineraId);
        if (existe == null || existe == 0) {
            jdbcTemplate.update(
                    "INSERT INTO perfiles_cocinera (id, conjunto_residencial, verificada, pausada, es_destacada, promedio_calificacion, resenas_positivas) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    cocineraId, "Torres del Parque", true, false, false, 0.0, 0);
        }
    }

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
                LocalDateTime.now().plusHours(2),
                "Portería Torre 1",
                4.6789,
                -74.0567
        );

        mockMvc.perform(post("/api/v1/platos")
                        .header("X-Cocinera-Id", "11111111-1111-1111-1111-111111111111")
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
                "ab",
                "corta",
                "",
                null,
                List.of(),
                0,
                new BigDecimal("100"),
                null,
                "",
                null,
                null
        );

        mockMvc.perform(post("/api/v1/platos")
                        .header("X-Cocinera-Id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerPorId_conPlatoInexistente_debeRetornar404() throws Exception {
        mockMvc.perform(get("/api/v1/platos/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminar_conPlatoInexistente_debeRetornar404() throws Exception {
        mockMvc.perform(delete("/api/v1/platos/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}