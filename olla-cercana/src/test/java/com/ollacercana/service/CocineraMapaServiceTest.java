package com.ollacercana.service;

import com.ollacercana.controller.dtos.request.MapaCocinerasRequestDTO;
import com.ollacercana.controller.dtos.response.CocineraMapaResponseDTO;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.services.impl.CocineraMapaServiceImpl;
import com.ollacercana.core.util.GeoUtils;
import com.ollacercana.persistence.entities.CuentaEntity;
import com.ollacercana.persistence.entities.IdentidadEmbeddable;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.PlatoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CocineraMapaServiceTest {

    @Mock
    private PlatoRepository platoRepository;

    @Mock
    private PerfilCocineraRepository perfilCocineraRepository;

    @InjectMocks
    private CocineraMapaServiceImpl cocineraMapaService;

    private UUID cocineraCercanaId;
    private UUID cocineraLejanaId;
    private PlatoEntity platoCercano;
    private PlatoEntity platoLejano;

    @BeforeEach
    void setUp() {
        cocineraCercanaId = UUID.randomUUID();
        cocineraLejanaId = UUID.randomUUID();

        // Punto de referencia: (4.6789, -74.0567)
        // Plato cercano: ~500m
        platoCercano = PlatoEntity.builder()
                .id(UUID.randomUUID())
                .cocineraId(cocineraCercanaId)
                .nombre("Ajiaco Santafereño")
                .precioPorcion(new BigDecimal("18000"))
                .fotoUrl("https://fotos.com/ajiaco.jpg")
                .latitud(4.6810)
                .longitud(-74.0540)
                .porcionesTotales(5)
                .porcionesComprometidas(0)
                .estado(EstadoPlato.ACTIVO)
                .fechaExpiracion(LocalDateTime.now().plusHours(2))
                .build();

        // Plato lejano: ~15km de distancia
        platoLejano = PlatoEntity.builder()
                .id(UUID.randomUUID())
                .cocineraId(cocineraLejanaId)
                .nombre("Bandeja Paisa")
                .precioPorcion(new BigDecimal("22000"))
                .fotoUrl("https://fotos.com/bandeja.jpg")
                .latitud(4.8000)
                .longitud(-74.1500)
                .porcionesTotales(8)
                .porcionesComprometidas(1)
                .estado(EstadoPlato.ACTIVO)
                .fechaExpiracion(LocalDateTime.now().plusHours(3))
                .build();
    }

    @Test
    @DisplayName("OC-232 / OC-236: Filtra cocineras dentro del radio y excluye las que están fuera")
    void buscarCocinerasEnMapa_filtraPorRadio() {
        when(platoRepository.findActivosVigentes(eq(EstadoPlato.ACTIVO), any(LocalDateTime.class)))
                .thenReturn(List.of(platoCercano, platoLejano));

        PerfilCocineraEntity perfilCercano = PerfilCocineraEntity.builder()
                .id(cocineraCercanaId)
                .conjuntoResidencial("Torre 1")
                .cuenta(CuentaEntity.builder()
                        .identidad(IdentidadEmbeddable.builder().nombre("Doña Bertha").build())
                        .build())
                .build();
        when(perfilCocineraRepository.findById(cocineraCercanaId)).thenReturn(Optional.of(perfilCercano));

        MapaCocinerasRequestDTO request = MapaCocinerasRequestDTO.builder()
                .latitud(4.6789)
                .longitud(-74.0567)
                .radio(2000.0) // 2km
                .build();

        List<CocineraMapaResponseDTO> resultado = cocineraMapaService.buscarCocinerasEnMapa(request);

        assertEquals(1, resultado.size());
        CocineraMapaResponseDTO item = resultado.get(0);
        assertEquals(cocineraCercanaId, item.getCocineraId());
        assertEquals("Doña Bertha", item.getNombreCocinera());
        assertEquals("Ajiaco Santafereño", item.getNombrePlato());
        assertEquals(new BigDecimal("18000"), item.getPrecio());
        assertEquals("https://fotos.com/ajiaco.jpg", item.getFotoPlato());

        // OC-235: Coordenadas ofuscadas nunca son iguales a las reales
        assertNotEquals(platoCercano.getLatitud(), item.getLatitudOfuscada());
        assertNotEquals(platoCercano.getLongitud(), item.getLongitudOfuscada());

        // Distancia redondeada a múltiplos de 100m
        assertEquals(0, item.getDistanciaMetros() % 100);

        // OC-235: Verificación de determinismo (segunda llamada produce el mismo resultado)
        List<CocineraMapaResponseDTO> resultado2 = cocineraMapaService.buscarCocinerasEnMapa(request);
        assertEquals(item.getLatitudOfuscada(), resultado2.get(0).getLatitudOfuscada());
        assertEquals(item.getLongitudOfuscada(), resultado2.get(0).getLongitudOfuscada());

        // OC-235: Sal distinta produce resultado distinto para el mismo id
        cocineraMapaService.setOfuscacionSalt("sal-completamente-distinta");
        List<CocineraMapaResponseDTO> resultadoSalDistinta = cocineraMapaService.buscarCocinerasEnMapa(request);
        assertFalse(item.getLatitudOfuscada() == resultadoSalDistinta.get(0).getLatitudOfuscada()
                && item.getLongitudOfuscada() == resultadoSalDistinta.get(0).getLongitudOfuscada(),
                "Una sal distinta debe generar coordenadas ofuscadas diferentes");

        // Margen de ofuscación entre 100m y 300m
        double distOfuscacion = GeoUtils.calcularDistanciaEnMetros(
                platoCercano.getLatitud(), platoCercano.getLongitud(),
                item.getLatitudOfuscada(), item.getLongitudOfuscada());
        assertTrue(distOfuscacion >= GeoUtils.MARGEN_MIN_OFUSCACION_METROS * 0.99);
        assertTrue(distOfuscacion <= GeoUtils.MARGEN_MAX_OFUSCACION_METROS * 1.01);
    }

    @Test
    @DisplayName("OC-232 / OC-236: Retorna lista vacía si no hay platos dentro del radio o no hay ofertas activas")
    void buscarCocinerasEnMapa_listaVaciaSinOfertas() {
        when(platoRepository.findActivosVigentes(eq(EstadoPlato.ACTIVO), any(LocalDateTime.class)))
                .thenReturn(List.of(platoLejano));

        MapaCocinerasRequestDTO request = MapaCocinerasRequestDTO.builder()
                .latitud(4.6789)
                .longitud(-74.0567)
                .radio(1000.0) // 1km, platoLejano está a ~15km
                .build();

        List<CocineraMapaResponseDTO> resultado = cocineraMapaService.buscarCocinerasEnMapa(request);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("OC-232: Si una cocinera tiene múltiples platos dentro del área, se agrupa y aparece una sola vez")
    void buscarCocinerasEnMapa_agrupaPorCocinera() {
        PlatoEntity otroPlatoMismaCocinera = PlatoEntity.builder()
                .id(UUID.randomUUID())
                .cocineraId(cocineraCercanaId)
                .nombre("Postre Natas")
                .precioPorcion(new BigDecimal("7000"))
                .latitud(4.6810)
                .longitud(-74.0540)
                .porcionesTotales(4)
                .porcionesComprometidas(0)
                .estado(EstadoPlato.ACTIVO)
                .fechaExpiracion(LocalDateTime.now().plusHours(2))
                .build();

        when(platoRepository.findActivosVigentes(eq(EstadoPlato.ACTIVO), any(LocalDateTime.class)))
                .thenReturn(List.of(platoCercano, otroPlatoMismaCocinera));
        when(perfilCocineraRepository.findById(cocineraCercanaId))
                .thenReturn(Optional.of(PerfilCocineraEntity.builder().id(cocineraCercanaId).conjuntoResidencial("Conjunto Norte").build()));

        MapaCocinerasRequestDTO request = MapaCocinerasRequestDTO.builder()
                .latitud(4.6789)
                .longitud(-74.0567)
                .radio(3000.0)
                .build();

        List<CocineraMapaResponseDTO> resultado = cocineraMapaService.buscarCocinerasEnMapa(request);

        assertEquals(1, resultado.size());
        assertEquals(cocineraCercanaId, resultado.get(0).getCocineraId());
        // Sin nombre en la cuenta, se muestra el conjunto residencial de la cocinera
        assertEquals("Conjunto Norte", resultado.get(0).getNombreCocinera());
    }
}
