package com.ollacercana.controller;

import com.ollacercana.domain.*;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.PlatoRepository;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlatoRepository platoRepository;

    @Autowired
    private PerfilCocineraRepository perfilRepository;

    @BeforeEach
    void setUp() {
        platoRepository.deleteAll();
        perfilRepository.deleteAll();

                                                       
        PerfilCocinera perfil = PerfilCocinera.builder()
                .conjuntoResidencial("Torres del Sol")
                .numeroNequi("3001234567")
                .numeroDaviplata("3007654321")
                .verificada(true)
                .build();
        PerfilCocinera guardado = perfilRepository.save(perfil);

                                                 
        Plato plato = Plato.builder()
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
                        .param("latitud", "4.6790")
                        .param("longitud", "-74.0560")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Sancocho de Pollo"))
                .andExpect(jsonPath("$[0].conjunto").value("Torres del Sol"))
                .andExpect(jsonPath("$[0].distanciaAproximada").exists())
                .andExpect(jsonPath("$[0].porcionesDisponibles").value(4))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString().toLowerCase();

                                                                                     
        assertFalse(jsonResponse.contains("puntoentrega"), "No debe contener 'puntoEntrega'");
        assertFalse(jsonResponse.contains("torre 3"), "No debe exponer la torre");
        assertFalse(jsonResponse.contains("apto"), "No debe exponer el número de apartamento");
        assertFalse(jsonResponse.contains("calle 123"), "No debe exponer la dirección exacta");
        assertFalse(jsonResponse.contains("3001234567"), "No debe exponer número de celular/nequi");
    }
}