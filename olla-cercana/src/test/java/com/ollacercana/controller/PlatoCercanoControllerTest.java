package com.ollacercana.controller;

import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.RestriccionAlimentaria;
import com.ollacercana.core.models.enums.TipoComida;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.PlatoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PlatoCercanoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PlatoRepository platoRepository;
    @Autowired private PerfilCocineraRepository perfilRepository;

    @BeforeEach
    void setUp() {
        platoRepository.deleteAll();
        perfilRepository.deleteAll();
        
        PerfilCocineraEntity perfil = PerfilCocineraEntity.builder()
                .conjuntoResidencial("Torres del Sol")
                .numeroNequi("3001234567")
                .numeroDaviplata("3007654321")
                .verificada(true)
                .pausada(false)
                .esDestacada(false)
                .promedioCalificacion(0.0)
                .resenasPositivas(0)
                .build();
        PerfilCocineraEntity guardado = perfilRepository.save(perfil);

        PlatoEntity plato = PlatoEntity.builder()
                .id(UUID.randomUUID())
                .cocineraId(guardado.getId())
                .nombre("Sancocho de Pollo")
                .descripcion("Delicioso sancocho casero")
                .fotoUrl("https://fotos.com/sancocho.jpg")
                .tipoComida(TipoComida.ALMUERZO)
                .restricciones(List.of(RestriccionAlimentaria.SIN_GLUTEN))
                .porcionesTotales(5)
                .porcionesComprometidas(1)
                .precioPorcion(new BigDecimal("15000"))
                .estado(EstadoPlato.ACTIVO)
                .fechaPublicacion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusHours(3))
                .puntoEntrega("Torre 3 Apto 502, Calle 123 #45-67")
                .latitud(4.6789)
                .longitud(-74.0567)
                .build();
        platoRepository.save(plato);
    }

    @Test
    @DisplayName("Platos Cercanos - Verifica DTO y no exposición de datos sensibles (RN-05)")
    void listarCercanos_NoDebeExponerDireccionCelularNiApto() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/platos/cercanos")
                        .param("lat", "4.6790")           // ← lat, no latitud
                        .param("lng", "-74.0560")         // ← lng, no longitud
                        .param("radioMetros", "1000")     // ← opcional pero recomendado
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].nombre").value("Sancocho de Pollo"))   // ← .contenido[0]
                .andExpect(jsonPath("$.contenido[0].conjunto").value("Torres del Sol"))    // ← .contenido[0]
                .andExpect(jsonPath("$.contenido[0].distanciaAproximada").exists())
                .andExpect(jsonPath("$.contenido[0].porcionesDisponibles").value(4))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString().toLowerCase();

        assertFalse(jsonResponse.contains("puntoentrega"), "No debe contener 'puntoEntrega'");
        assertFalse(jsonResponse.contains("torre 3"), "No debe exponer la torre");
        assertFalse(jsonResponse.contains("apto"), "No debe exponer el número de apartamento");
        assertFalse(jsonResponse.contains("calle 123"), "No debe exponer la dirección exacta");
        assertFalse(jsonResponse.contains("3001234567"), "No debe exponer número de celular/nequi");
    }
}