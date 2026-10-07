package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.model.domain.RestriccionAlimentaria;
import com.ollacercana.model.domain.Rol;
import com.ollacercana.model.domain.TipoAjustePorciones;
import com.ollacercana.model.domain.TipoComida;
import com.ollacercana.model.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.model.dto.request.PlatoRequestDTO;
import com.ollacercana.security.UsuarioActual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "cocinera@ollacercana.com", roles = {"COCINERA"})
class PlatoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;
    @MockBean private UsuarioActual usuarioActual;

    private static final UUID COCINERA_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void asegurarCocineraSembrada() {
        lenient().when(usuarioActual.getCocineraId()).thenReturn(COCINERA_ID);
        lenient().when(usuarioActual.getCuentaId()).thenReturn(1L);
        lenient().when(usuarioActual.tieneRol(Rol.ADMIN)).thenReturn(false);

        Integer existe = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM perfiles_cocinera WHERE id = ?", Integer.class, COCINERA_ID);
        if (existe == null || existe == 0) {
            jdbcTemplate.update(
                    "INSERT INTO perfiles_cocinera (id, conjunto_residencial, verificada, pausada, es_destacada, promedio_calificacion, resenas_positivas) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    COCINERA_ID, "Torres del Parque", true, false, false, 0.0, 0);
        }
    }
    @BeforeEach
void setUp() {
    // Limpia la tabla de platos para evitar acumulación entre tests
    jdbcTemplate.execute("DELETE FROM plato_restricciones");
        jdbcTemplate.execute("DELETE FROM platos");

    lenient().when(usuarioActual.getCocineraId()).thenReturn(COCINERA_ID);
    lenient().when(usuarioActual.getCuentaId()).thenReturn(1L);
    lenient().when(usuarioActual.tieneRol(Rol.ADMIN)).thenReturn(false);

    Integer existe = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM perfiles_cocinera WHERE id = ?",
            Integer.class, COCINERA_ID);
    if (existe == null || existe == 0) {
        jdbcTemplate.update(
                "INSERT INTO perfiles_cocinera (id, conjunto_residencial, verificada, pausada, es_destacada, promedio_calificacion, resenas_positivas) VALUES (?, ?, ?, ?, ?, ?, ?)",
                COCINERA_ID, "Torres del Parque", true, false, false, 0.0, 0);
    }
}

    @Test
    void publicar_debeRetornar201() throws Exception {
        PlatoRequestDTO request = new PlatoRequestDTO(
                "Arroz con pollo", "Arroz con pollo criollo con ensalada",
                "http://foto.com/arroz.jpg", TipoComida.ALMUERZO,
                List.of(RestriccionAlimentaria.SIN_GLUTEN),
                10, new BigDecimal("12000.00"),
                LocalDateTime.now().plusHours(2),
                "Portería Torre 1", 4.6789, -74.0567
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
                "ab", "corta", "", null, List.of(), 0,
                new BigDecimal("100"), null, "", null, null
        );

        mockMvc.perform(post("/api/v1/platos")
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

    @Test
    void obtenerPorId_conPlatoExistente_debeRetornar200() throws Exception {
        PlatoRequestDTO request = new PlatoRequestDTO(
                "Ajiaco", "Sopa con pollo y tres papas", "http://foto.com/a.jpg",
                TipoComida.ALMUERZO, List.of(), 5, new BigDecimal("14000.00"),
                LocalDateTime.now().plusHours(2), "Portería", 4.6789, -74.0567
        );

        String json = mockMvc.perform(post("/api/v1/platos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(json).get("id").asText();

        mockMvc.perform(get("/api/v1/platos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ajiaco"));
    }

    @Test
        void actualizarDisponibilidad_debeRetornar200() throws Exception {
        PlatoRequestDTO request = new PlatoRequestDTO(
                "Mondongo", "Sopa típica tradicional", "http://foto.com/m.jpg",
                TipoComida.ALMUERZO, List.of(), 6, new BigDecimal("15000.00"),
                LocalDateTime.now().plusHours(2), "Portería", 4.6789, -74.0567
        );

        String json = mockMvc.perform(post("/api/v1/platos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(json).get("id").asText();

        // ✅ Lee la versión desde el repositorio, no del JSON.
        UUID platoUuid = UUID.fromString(id);
        Integer version = jdbcTemplate.queryForObject(
                "SELECT version FROM platos WHERE id = ?", Integer.class, platoUuid);
        if (version == null) version = 0;

        AjusteDisponibilidadRequest ajuste = new AjusteDisponibilidadRequest(
                TipoAjustePorciones.AUMENTAR, 2, "Cocinó más", version);

        mockMvc.perform(patch("/api/v1/platos/{id}/disponibilidad", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ajuste)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porcionesTotales").value(8));
        }

    @Test
    void eliminar_conPlatoExistente_debeRetornar204() throws Exception {
        PlatoRequestDTO request = new PlatoRequestDTO(
                "Postre Natas", "Dulce casero tradicional", "http://foto.com/p.jpg",
                TipoComida.POSTRE, List.of(), 4, new BigDecimal("8000.00"),
                LocalDateTime.now().plusHours(2), "Portería", 4.6789, -74.0567
        );

        String json = mockMvc.perform(post("/api/v1/platos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(json).get("id").asText();

        mockMvc.perform(delete("/api/v1/platos/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
        @DisplayName("HU-06: consulta platos cercanos con GPS → 200 y lista")
        void cercanos_conGps_debeRetornar200() throws Exception {
        // Primero publicar un plato con coordenadas
        PlatoRequestDTO request = new PlatoRequestDTO(
                "Ajiaco cercano", "Sopa tradicional", "http://foto.com/a.jpg",
                TipoComida.ALMUERZO, List.of(),
                5, new BigDecimal("14000.00"),
                LocalDateTime.now().plusHours(2),
                "Portería Torre 1", 4.6789, -74.0567
        );

        mockMvc.perform(post("/api/v1/platos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/platos/cercanos")
                        .param("lat", "4.6789")
                        .param("lng", "-74.0567")
                        .param("radioMetros", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray())
                .andExpect(jsonPath("$.page").value(0));
        }

        @Test
        @DisplayName("HU-06 Escenario 3: sin platos cerca → 200 con contenido vacío")
        void cercanos_sinResultados_debeRetornar200Vacio() throws Exception {
        mockMvc.perform(get("/api/v1/platos/cercanos")
                        .param("lat", "0.0")
                        .param("lng", "0.0")
                        .param("radioMetros", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isEmpty())
                .andExpect(jsonPath("$.totalElementos").value(0));
        }

        @Test
        @DisplayName("HU-06: sin lat → 400 Bad Request")
        void cercanos_sinLat_debeRetornar400() throws Exception {
        mockMvc.perform(get("/api/v1/platos/cercanos")
                        .param("lng", "-74.0567"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("HU-07: filtrar por tipoComida")
        void cercanos_porTipoComida() throws Exception {
        PlatoRequestDTO request = new PlatoRequestDTO(
                "Postre de natas", "Dulce típico", "http://foto.com/p.jpg",
                TipoComida.POSTRE, List.of(),
                4, new BigDecimal("8000.00"),
                LocalDateTime.now().plusHours(2),
                "Portería", 4.6789, -74.0567
        );

        mockMvc.perform(post("/api/v1/platos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/platos/cercanos")
                        .param("lat", "4.6789")
                        .param("lng", "-74.0567")
                        .param("tipoComida", "POSTRE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].tipoComida").value("POSTRE"));
        }
}