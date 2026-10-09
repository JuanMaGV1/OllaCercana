package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.request.MapaCocinerasRequestDTO;
import com.ollacercana.controller.dtos.response.CocineraMapaResponseDTO;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.services.impl.CocineraMapaServiceImpl;
import com.ollacercana.persistence.entities.CuentaEntity;
import com.ollacercana.persistence.entities.IdentidadEmbeddable;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.mappers.PlatoEntityMapper;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.PlatoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CocineraMapaServiceTest {

    @Mock private PlatoRepository platoRepository;
    @Mock private PerfilCocineraRepository perfilCocineraRepository;
    @Mock private PlatoEntityMapper platoEntityMapper;

    private CocineraMapaServiceImpl cocineraMapaService;

    private UUID cocineraCercanaId;
    private UUID cocineraLejanaId;
    private PlatoEntity platoCercanoEntity;
    private PlatoEntity platoLejanoEntity;

    @BeforeEach
    void setUp() {
        cocineraCercanaId = UUID.randomUUID();
        cocineraLejanaId = UUID.randomUUID();

        platoCercanoEntity = PlatoEntity.builder()
                .id(UUID.randomUUID())
                .cocineraId(cocineraCercanaId)
                .nombre("Ajiaco Santafereño")
                .precioPorcion(new BigDecimal("18000"))
                .fotoUrl("https://fotos.com/ajiaco.jpg")
                .latitud(4.6810).longitud(-74.0540)
                .porcionesTotales(5).porcionesComprometidas(0)
                .estado(EstadoPlato.ACTIVO)
                .fechaExpiracion(LocalDateTime.now().plusHours(2))
                .build();

        platoLejanoEntity = PlatoEntity.builder()
                .id(UUID.randomUUID())
                .cocineraId(cocineraLejanaId)
                .nombre("Bandeja Paisa")
                .precioPorcion(new BigDecimal("22000"))
                .fotoUrl("https://fotos.com/bandeja.jpg")
                .latitud(4.8000).longitud(-74.1500)
                .porcionesTotales(8).porcionesComprometidas(1)
                .estado(EstadoPlato.ACTIVO)
                .fechaExpiracion(LocalDateTime.now().plusHours(3))
                .build();

        cocineraMapaService = new CocineraMapaServiceImpl(
                platoRepository, perfilCocineraRepository, platoEntityMapper);

        // ✅ Stub genérico: cualquier PlatoEntity → su dominio equivalente
        lenient().when(platoEntityMapper.toDomain(any(PlatoEntity.class))).thenAnswer(inv -> {
            PlatoEntity e = inv.getArgument(0);
            return Plato.builder()
                    .id(e.getId())
                    .cocineraId(e.getCocineraId())
                    .nombre(e.getNombre())
                    .precioPorcion(e.getPrecioPorcion())
                    .fotoUrl(e.getFotoUrl())
                    .latitud(e.getLatitud()).longitud(e.getLongitud())
                    .porcionesTotales(e.getPorcionesTotales())
                    .porcionesComprometidas(e.getPorcionesComprometidas())
                    .estado(e.getEstado())
                    .fechaExpiracion(e.getFechaExpiracion())
                    .build();
        });
    }

    @Test
    @DisplayName("OC-232 / OC-236: Filtra cocineras dentro del radio y excluye las que están fuera")
    void buscarCocinerasEnMapa_filtraPorRadio() {
        when(platoRepository.findActivosVigentes(eq(EstadoPlato.ACTIVO), any(LocalDateTime.class)))
                .thenReturn(List.of(platoCercanoEntity, platoLejanoEntity));

        PerfilCocineraEntity perfilCercano = PerfilCocineraEntity.builder()
                .id(cocineraCercanaId)
                .conjuntoResidencial("Torre 1")
                .cuenta(CuentaEntity.builder()
                        .id(1L)
                        .identidad(IdentidadEmbeddable.builder().nombre("Doña Bertha").build())
                        .build())
                .build();
        lenient().when(perfilCocineraRepository.findById(cocineraCercanaId))
                .thenReturn(Optional.of(perfilCercano));

        MapaCocinerasRequestDTO request = MapaCocinerasRequestDTO.builder()
                .latitud(4.6789).longitud(-74.0567).radio(2000.0)
                .build();

        List<CocineraMapaResponseDTO> resultado = cocineraMapaService.buscarCocinerasEnMapa(request);

        assertEquals(1, resultado.size());
        CocineraMapaResponseDTO item = resultado.get(0);
        assertEquals(cocineraCercanaId, item.getCocineraId());
        assertEquals("Doña Bertha", item.getNombreCocinera());
        assertEquals("Ajiaco Santafereño", item.getNombrePlato());
        assertEquals(new BigDecimal("18000"), item.getPrecio());
        assertEquals("https://fotos.com/ajiaco.jpg", item.getFotoPlato());

        assertNotEquals(platoCercanoEntity.getLatitud(), item.getLatitudOfuscada());
        assertNotEquals(platoCercanoEntity.getLongitud(), item.getLongitudOfuscada());
        assertEquals(0, item.getDistanciaMetros() % 100);
    }

    @Test
    @DisplayName("OC-232 / OC-236: Retorna lista vacía si no hay platos dentro del radio")
    void buscarCocinerasEnMapa_listaVaciaSinOfertas() {
        when(platoRepository.findActivosVigentes(eq(EstadoPlato.ACTIVO), any(LocalDateTime.class)))
                .thenReturn(List.of(platoLejanoEntity));

        MapaCocinerasRequestDTO request = MapaCocinerasRequestDTO.builder()
                .latitud(4.6789).longitud(-74.0567).radio(1000.0)
                .build();

        List<CocineraMapaResponseDTO> resultado = cocineraMapaService.buscarCocinerasEnMapa(request);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("OC-232: Si una cocinera tiene múltiples platos dentro del área, se agrupa")
    void buscarCocinerasEnMapa_agrupaPorCocinera() {
        PlatoEntity otroPlatoEntity = PlatoEntity.builder()
                .id(UUID.randomUUID())
                .cocineraId(cocineraCercanaId)
                .nombre("Postre Natas")
                .precioPorcion(new BigDecimal("7000"))
                .latitud(4.6810).longitud(-74.0540)
                .porcionesTotales(4).porcionesComprometidas(0)
                .estado(EstadoPlato.ACTIVO)
                .fechaExpiracion(LocalDateTime.now().plusHours(2))
                .build();

        when(platoRepository.findActivosVigentes(eq(EstadoPlato.ACTIVO), any(LocalDateTime.class)))
                .thenReturn(List.of(platoCercanoEntity, otroPlatoEntity));

        PerfilCocineraEntity perfil = PerfilCocineraEntity.builder()
                .id(cocineraCercanaId)
                .conjuntoResidencial("Conjunto Norte")
                .build();
        lenient().when(perfilCocineraRepository.findById(cocineraCercanaId)).thenReturn(Optional.of(perfil));

        MapaCocinerasRequestDTO request = MapaCocinerasRequestDTO.builder()
                .latitud(4.6789).longitud(-74.0567).radio(3000.0)
                .build();

        List<CocineraMapaResponseDTO> resultado = cocineraMapaService.buscarCocinerasEnMapa(request);

        assertEquals(1, resultado.size());
        assertEquals(cocineraCercanaId, resultado.get(0).getCocineraId());
    }
}