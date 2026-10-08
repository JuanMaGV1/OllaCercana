package com.ollacercana.service;

import com.ollacercana.controller.dtos.response.CocineraMapaResponseDTO;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.services.impl.CocineraMapaServiceImpl;
import com.ollacercana.core.util.GeoUtils;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.PlatoRepository;
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

    @Mock private PlatoRepository platoRepository;
    @Mock private PerfilCocineraRepository perfilCocineraRepository;

    @InjectMocks private CocineraMapaServiceImpl service;

    private static final double LAT_USUARIO = 4.6789;
    private static final double LNG_USUARIO = -74.0567;

    @Test
    @DisplayName("OC-236: Cocinera dentro del radio aparece con coordenadas ofuscadas y distancia redondeada")
    void buscarEnMapa_CocineraDentroDelRadio_RetornaCorrectamente() {
        UUID cocineraId = UUID.randomUUID();
        double latPlato = 4.6800; // ~140 metros de distancia
        double lngPlato = -74.0560;

        PlatoEntity plato = PlatoEntity.builder()
                .id(UUID.randomUUID())
                .cocineraId(cocineraId)
                .nombre("Sancocho")
                .fotoUrl("https://fotos.com/sancocho.jpg")
                .precioPorcion(new BigDecimal("15000"))
                .latitud(latPlato)
                .longitud(lngPlato)
                .porcionesTotales(5)
                .porcionesComprometidas(1)
                .estado(EstadoPlato.ACTIVO)
                .fechaExpiracion(LocalDateTime.now().plusHours(2))
                .build();

        PerfilCocineraEntity perfil = PerfilCocineraEntity.builder()
                .id(cocineraId)
                .conjuntoResidencial("Torres del Parque")
                .pausada(false)
                .build();

        when(platoRepository.findOfertasActivasConUbicacion(eq(EstadoPlato.ACTIVO), any(LocalDateTime.class)))
                .thenReturn(List.of(plato));
        when(perfilCocineraRepository.findById(cocineraId)).thenReturn(Optional.of(perfil));

        List<CocineraMapaResponseDTO> resultado = service.buscarCocinerasEnMapa(LAT_USUARIO, LNG_USUARIO, 1000.0);

        assertEquals(1, resultado.size());
        CocineraMapaResponseDTO dto = resultado.get(0);

        assertEquals(cocineraId, dto.getCocineraId());
        assertEquals("Torres del Parque", dto.getNombreCocinera());
        assertEquals("Sancocho", dto.getPlatoNombre());
        assertEquals(new BigDecimal("15000"), dto.getPrecio());

        // Verificaciones clave de OC-236:
        // 1. Las coordenadas ofuscadas NO son iguales a las reales
        assertNotEquals(latPlato, dto.getLatitud());
        assertNotEquals(lngPlato, dto.getLongitud());

        // 2. Quedan dentro del margen de seguridad definido (~100m)
        double distOfuscacion = GeoUtils.calcularDistanciaEnMetros(latPlato, lngPlato, dto.getLatitud(), dto.getLongitud());
        assertTrue(distOfuscacion <= 150.0, "La ofuscación debe respetar el margen");

        // 3. La distancia estimada está redondeada a múltiplos de 100m
        assertEquals(0, dto.getDistanciaMetros() % 100);
    }

    @Test
    @DisplayName("OC-236: Cocinera fuera del radio es filtrada y no aparece")
    void buscarEnMapa_CocineraFueraDelRadio_NoAparece() {
        UUID cocineraId = UUID.randomUUID();
        // A varios kilómetros de distancia
        PlatoEntity platoLejano = PlatoEntity.builder()
                .id(UUID.randomUUID())
                .cocineraId(cocineraId)
                .latitud(4.7500)
                .longitud(-74.0200)
                .porcionesTotales(5)
                .porcionesComprometidas(0)
                .estado(EstadoPlato.ACTIVO)
                .fechaExpiracion(LocalDateTime.now().plusHours(2))
                .build();

        when(platoRepository.findOfertasActivasConUbicacion(eq(EstadoPlato.ACTIVO), any(LocalDateTime.class)))
                .thenReturn(List.of(platoLejano));

        List<CocineraMapaResponseDTO> resultado = service.buscarCocinerasEnMapa(LAT_USUARIO, LNG_USUARIO, 500.0);

        assertTrue(resultado.isEmpty(), "No debe incluir cocineras fuera del radio");
    }

    @Test
    @DisplayName("OC-236: Si no hay ofertas activas retorna lista vacía")
    void buscarEnMapa_SinOfertas_RetornaListaVacia() {
        when(platoRepository.findOfertasActivasConUbicacion(eq(EstadoPlato.ACTIVO), any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<CocineraMapaResponseDTO> resultado = service.buscarCocinerasEnMapa(LAT_USUARIO, LNG_USUARIO, 2000.0);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("OC-236: Parámetros nulos lanzan IllegalArgumentException")
    void buscarEnMapa_CoordenadasNulas_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> service.buscarCocinerasEnMapa(null, LNG_USUARIO, 2000.0));
        assertThrows(IllegalArgumentException.class, () -> service.buscarCocinerasEnMapa(LAT_USUARIO, null, 2000.0));
    }
}