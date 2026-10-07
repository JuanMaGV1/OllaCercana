package com.ollacercana.service;

import com.ollacercana.mapper.PlatoEntityMapper;
import com.ollacercana.model.domain.EstadoPlato;
import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.domain.RestriccionAlimentaria;
import com.ollacercana.model.domain.TipoComida;
import com.ollacercana.model.dto.request.ConsultaPlatosRequest;
import com.ollacercana.model.dto.response.PaginaResponseDTO;
import com.ollacercana.model.dto.response.PlatoCercanoResponseDTO;
import com.ollacercana.persistence.entity.PerfilCocineraEntity;
import com.ollacercana.persistence.entity.PlatoEntity;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.service.impl.PlatoServiceImpl;
import com.ollacercana.validator.PlatoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatoServiceImplCercanosTest {

    @Mock private PlatoRepository platoRepository;
    @Mock private PlatoValidator validator;
    @Mock private PlatoEntityMapper entityMapper;
    @Mock private PerfilCocineraRepository perfilCocineraRepository;

    private PlatoServiceImpl service;

    private static final double LAT = 4.6533;
    private static final double LNG = -74.0836;

    @BeforeEach
    void setUp() {
        service = new PlatoServiceImpl(
        platoRepository,
        validator,
        entityMapper,
        perfilCocineraRepository
        );
        lenient().when(perfilCocineraRepository.findById(any()))
        .thenReturn(java.util.Optional.of(
                PerfilCocineraEntity.builder()
                        .id(UUID.randomUUID())
                        .conjuntoResidencial("Torres del Parque")
                        .build()));
    }

    // ==================== Helpers ====================

    private PlatoEntity entidad(UUID id, double lat, double lng, TipoComida tipo) {
        return PlatoEntity.builder()
                .id(id).nombre("Ajiaco").latitud(lat).longitud(lng)
                .porcionesTotales(5).porcionesComprometidas(0)
                .precioPorcion(new BigDecimal("16000"))
                .estado(EstadoPlato.ACTIVO).tipoComida(tipo)
                .fechaExpiracion(LocalDateTime.now().plusHours(3))
                .build();
    }

    private Plato dominio(UUID id, double lat, double lng, TipoComida tipo,
                          List<RestriccionAlimentaria> restricciones) {
        return Plato.builder()
                .id(id).nombre("Ajiaco").latitud(lat).longitud(lng)
                .porcionesTotales(5).porcionesComprometidas(0)
                .precioPorcion(new BigDecimal("16000"))
                .estado(EstadoPlato.ACTIVO).tipoComida(tipo)
                .cocineraId(UUID.randomUUID())
                .fechaExpiracion(LocalDateTime.now().plusHours(3))
                .puntoEntrega("Torres del Parque")
                .restricciones(restricciones)
                .build();
    }

    private ConsultaPlatosRequest request() {
        ConsultaPlatosRequest r = new ConsultaPlatosRequest();
        r.setLat(LAT);
        r.setLng(LNG);
        return r;
    }

    private Page<Object[]> pageDe(PlatoEntity... entidades) {
        List<Object[]> filas = Arrays.stream(entidades)
                .map(e -> new Object[]{e, 800.0})
                .toList();
        return new PageImpl<>(filas);
    }

    // ============================================================
    // HU-06 — Escenario 1: camino feliz con GPS
    // ============================================================

    @Nested
    @DisplayName("HU-06 — Escenario 1: consulta con GPS")
    class Escenario1 {

        @Test
        @DisplayName("Retorna platos con distancia redondeada y ordenados")
        void retornaPlatosOrdenados() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            PlatoEntity e1 = entidad(id1, 4.6600, LNG, TipoComida.ALMUERZO);
            PlatoEntity e2 = entidad(id2, 4.6540, LNG, TipoComida.ALMUERZO);

            when(platoRepository.buscarCercanosConDistancia(
                    any(), any(), any(), any(), any(), any(Pageable.class)))
                    .thenReturn(pageDe(e1, e2));
            when(entityMapper.toDomain(e1))
                    .thenReturn(dominio(id1, 4.6600, LNG, TipoComida.ALMUERZO, List.of()));
            when(entityMapper.toDomain(e2))
                    .thenReturn(dominio(id2, 4.6540, LNG, TipoComida.ALMUERZO, List.of()));

            ConsultaPlatosRequest req = request();
            req.setRadioMetros(1000);

            PaginaResponseDTO<PlatoCercanoResponseDTO> r = service.consultarCercanos(req);

            assertNotNull(r);
            assertEquals(2, r.getContenido().size());
            assertTrue(r.getContenido().get(0).getDistanciaAproximada()
                    <= r.getContenido().get(1).getDistanciaAproximada());
        }

        @Test
        @DisplayName("Distancia es múltiplo de 100 (RN-05)")
        void distanciaMultiploDe100() {
            UUID id = UUID.randomUUID();
            PlatoEntity e = entidad(id, 4.6600, LNG, TipoComida.ALMUERZO);

            when(platoRepository.buscarCercanosConDistancia(
                    any(), any(), any(), any(), any(), any(Pageable.class)))
                    .thenReturn(pageDe(e));
            when(entityMapper.toDomain(e))
                    .thenReturn(dominio(id, 4.6600, LNG, TipoComida.ALMUERZO, List.of()));

            var r = service.consultarCercanos(request());

            Integer d = r.getContenido().get(0).getDistanciaAproximada();
            assertNotNull(d);
            assertEquals(0, d % 100);
        }

        @Test
        @DisplayName("Mapea todos los campos del DTO")
        void mapeaTodosLosCampos() {
            UUID id = UUID.randomUUID();
            PlatoEntity e = entidad(id, 4.6540, LNG, TipoComida.ALMUERZO);
            Plato d = dominio(id, 4.6540, LNG, TipoComida.ALMUERZO,
                    List.of(RestriccionAlimentaria.SIN_GLUTEN));

            when(platoRepository.buscarCercanosConDistancia(
                    any(), any(), any(), any(), any(), any(Pageable.class)))
                    .thenReturn(pageDe(e));
            when(entityMapper.toDomain(e)).thenReturn(d);

            var r = service.consultarCercanos(request());
            PlatoCercanoResponseDTO dto = r.getContenido().get(0);

            assertEquals(id, dto.getId());
            assertEquals("Ajiaco", dto.getNombre());
            assertEquals(TipoComida.ALMUERZO, dto.getTipoComida());
            assertEquals(new BigDecimal("16000"), dto.getPrecioPorcion());
            assertEquals(5, dto.getPorcionesDisponibles());
            assertEquals("Torres del Parque", dto.getConjunto());
            assertEquals(List.of(RestriccionAlimentaria.SIN_GLUTEN), dto.getRestricciones());
            assertNotNull(dto.getTiempoRestante());
        }
    }

    // ============================================================
    // HU-06 — Escenario 3: sin resultados
    // ============================================================

    @Test
    @DisplayName("HU-06 Escenario 3: página vacía con metadatos correctos")
    void sinResultados() {
        when(platoRepository.buscarCercanosConDistancia(
                any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        var r = service.consultarCercanos(request());

        assertNotNull(r);
        assertTrue(r.getContenido().isEmpty());
        assertEquals(0, r.getTotalElementos());
        assertFalse(r.isHayMas());
    }

    // ============================================================
    // HU-07 — Filtro por tipo de menú
    // ============================================================

    @Test
    @DisplayName("HU-07: propaga tipoComida al repositorio")
    void propagaTipoComida() {
        when(platoRepository.buscarCercanosConDistancia(
                any(), any(), any(), eq(TipoComida.CENA), any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        ConsultaPlatosRequest req = request();
        req.setTipoComida(TipoComida.CENA);

        service.consultarCercanos(req);

        verify(platoRepository).buscarCercanosConDistancia(
                any(), any(), any(), eq(TipoComida.CENA), any(), any(Pageable.class));
    }

    // ============================================================
    // HU-07 — Filtro por restricciones (enum)
    // ============================================================

    @Test
    @DisplayName("HU-07: plato con la restricción pasa el filtro")
    void platoCumpleRestriccion() {
        UUID id = UUID.randomUUID();
        PlatoEntity e = entidad(id, 4.6540, LNG, TipoComida.ALMUERZO);

        when(platoRepository.buscarCercanosConDistancia(
                any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageDe(e));
        when(entityMapper.toDomain(e))
                .thenReturn(dominio(id, 4.6540, LNG, TipoComida.ALMUERZO,
                        List.of(RestriccionAlimentaria.VEGETARIANO)));

        ConsultaPlatosRequest req = request();
        req.setRestricciones(List.of(RestriccionAlimentaria.VEGETARIANO));

        var r = service.consultarCercanos(req);
        assertEquals(1, r.getContenido().size());
    }

    @Test
    @DisplayName("HU-07: plato sin la restricción no aparece")
    void platoNoCumpleRestriccion() {
        UUID id = UUID.randomUUID();
        PlatoEntity e = entidad(id, 4.6540, LNG, TipoComida.ALMUERZO);

        when(platoRepository.buscarCercanosConDistancia(
                any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageDe(e));
        when(entityMapper.toDomain(e))
                .thenReturn(dominio(id, 4.6540, LNG, TipoComida.ALMUERZO,
                        List.of(RestriccionAlimentaria.SIN_GLUTEN)));

        ConsultaPlatosRequest req = request();
        req.setRestricciones(List.of(RestriccionAlimentaria.VEGETARIANO));

        var r = service.consultarCercanos(req);
        assertTrue(r.getContenido().isEmpty());
    }

    @Test
    @DisplayName("HU-07 Escenario 3: sin restricciones → catálogo completo")
    void sinRestriccionesCatalogoCompleto() {
        UUID id = UUID.randomUUID();
        PlatoEntity e = entidad(id, 4.6540, LNG, TipoComida.ALMUERZO);

        when(platoRepository.buscarCercanosConDistancia(
                any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageDe(e));
        when(entityMapper.toDomain(e))
                .thenReturn(dominio(id, 4.6540, LNG, TipoComida.ALMUERZO, List.of()));

        ConsultaPlatosRequest req = request();
        // Sin restricciones, sin tipoComida

        var r = service.consultarCercanos(req);
        assertEquals(1, r.getContenido().size());
    }

    // ============================================================
    // Radio
    // ============================================================

    @Nested
    @DisplayName("Normalización del radio")
    class Radio {

        @Test
        @DisplayName("Radio null → 2000 por defecto")
        void radioNull() {
            when(platoRepository.buscarCercanosConDistancia(
                    any(), any(), eq(2000), any(), any(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            ConsultaPlatosRequest req = request();
            req.setRadioMetros(null);

            service.consultarCercanos(req);
        }

        @Test
        @DisplayName("Radio < 500 → 500")
        void radioChico() {
            when(platoRepository.buscarCercanosConDistancia(
                    any(), any(), eq(500), any(), any(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            ConsultaPlatosRequest req = request();
            req.setRadioMetros(100);

            service.consultarCercanos(req);
        }

        @Test
        @DisplayName("Radio > 2000 → 2000")
        void radioGrande() {
            when(platoRepository.buscarCercanosConDistancia(
                    any(), any(), eq(2000), any(), any(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            ConsultaPlatosRequest req = request();
            req.setRadioMetros(50000);

            service.consultarCercanos(req);
        }
    }
}