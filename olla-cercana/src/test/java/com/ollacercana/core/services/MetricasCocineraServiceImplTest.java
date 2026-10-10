package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.response.HistorialPaginadoResponseDTO;
import com.ollacercana.controller.dtos.response.MetricasCocineraResponseDTO;
import com.ollacercana.controller.handlers.exception.ReglaDeNegocioException;
import com.ollacercana.core.models.Cuenta;
import com.ollacercana.core.models.Identidad;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.Reserva;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.services.impl.MetricasCocineraServiceImpl;
import com.ollacercana.persistence.entities.CuentaEntity;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.entities.ReservaEntity;
import com.ollacercana.persistence.mappers.CuentaEntityMapper;
import com.ollacercana.persistence.mappers.PlatoEntityMapper;
import com.ollacercana.persistence.mappers.ReservaEntityMapper;
import com.ollacercana.persistence.repository.CuentaRepository;
import com.ollacercana.persistence.repository.PlatoPedidosProjection;
import com.ollacercana.persistence.repository.PlatoRepository;
import com.ollacercana.persistence.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
class MetricasCocineraServiceImplTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private PlatoRepository platoRepository;
    @Mock private CuentaRepository cuentaRepository;
    @Mock private ReservaEntityMapper reservaMapper;
    @Mock private PlatoEntityMapper platoMapper;
    @Mock private CuentaEntityMapper cuentaMapper;

    private MetricasCocineraServiceImpl service;

    private static final UUID COCINERA_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new MetricasCocineraServiceImpl(
                reservaRepository, platoRepository, cuentaRepository,
                reservaMapper, platoMapper, cuentaMapper);
    }

    // ============ obtenerMetricas ============

    @Test
    @DisplayName("Con ventas → devuelve ingresos, porciones y plato más pedido")
    void obtenerMetricas_conVentas() {
        when(reservaRepository.sumarIngresosPorPeriodo(eq(COCINERA_ID), eq(EstadoReserva.COMPLETADA), any(), any()))
                .thenReturn(new BigDecimal("120000.00"));
        when(reservaRepository.sumarPorcionesPorPeriodo(eq(COCINERA_ID), eq(EstadoReserva.COMPLETADA), any(), any()))
                .thenReturn(8L);

        UUID platoId = UUID.randomUUID();
        PlatoPedidosProjection proj = new PlatoPedidosProjection() {
            @Override public UUID getPlatoId() { return platoId; }
            @Override public Long getTotalPedidos() { return 4L; }
            @Override public Long getTotalPorciones() { return 8L; }
        };
        when(reservaRepository.findPlatosMasPedidos(eq(COCINERA_ID), eq(EstadoReserva.COMPLETADA),
                any(), any(), any(PageRequest.class)))
                .thenReturn(List.of(proj));

        PlatoEntity platoEntity = PlatoEntity.builder().id(platoId).nombre("Sancocho").build();
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(platoEntity));
        when(platoMapper.toDomain(platoEntity)).thenReturn(Plato.builder().id(platoId).nombre("Sancocho").build());

        MetricasCocineraResponseDTO result = service.obtenerMetricas(COCINERA_ID, null, null);

        assertNotNull(result);
        assertEquals(new BigDecimal("120000.00"), result.getIngresosReferenciales());
        assertEquals(8L, result.getPorcionesEntregadas());
        assertEquals("Sancocho", result.getPlatoMasPedido());
    }

    @Test
    @DisplayName("Sin ventas → ceros y plato vacío")
    void obtenerMetricas_sinVentas() {
        when(reservaRepository.sumarIngresosPorPeriodo(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(reservaRepository.sumarPorcionesPorPeriodo(any(), any(), any(), any()))
                .thenReturn(0L);
        when(reservaRepository.findPlatosMasPedidos(any(), any(), any(), any(), any(PageRequest.class)))
                .thenReturn(List.of());

        MetricasCocineraResponseDTO result = service.obtenerMetricas(COCINERA_ID, null, null);

        assertEquals(BigDecimal.ZERO, result.getIngresosReferenciales());
        assertEquals(0L, result.getPorcionesEntregadas());
        assertEquals("", result.getPlatoMasPedido());
    }

    @Test
    @DisplayName("Desde > hasta → ReglaDeNegocioException")
    void obtenerMetricas_periodoInvalido() {
        assertThrows(ReglaDeNegocioException.class,
                () -> service.obtenerMetricas(COCINERA_ID,
                        java.time.LocalDate.now(),
                        java.time.LocalDate.now().minusDays(1)));
    }

    // ============ obtenerHistorial ============

    @Test
    @DisplayName("Historial paginado → mapea entity a DTO con nombre del comprador y plato")
    void obtenerHistorial_paginado() {
        UUID reservaId = UUID.randomUUID();
        UUID platoId = UUID.randomUUID();

        ReservaEntity reservaEntity = ReservaEntity.builder()
                .id(reservaId)
                .platoId(platoId)
                .compradorId(1L)
                .cantidadPorciones(2)
                .fechaCompletada(LocalDateTime.now())
                .calificacion(5)
                .build();

        Reserva reservaDominio = Reserva.builder()
                .id(reservaId)
                .platoId(platoId)
                .compradorId(1L)
                .cantidadPorciones(2)
                .fechaCompletada(LocalDateTime.now())
                .calificacion(5)
                .build();

        Page<ReservaEntity> page = new PageImpl<>(List.of(reservaEntity),
                PageRequest.of(0, 10), 1);
        when(reservaRepository.findByCocineraIdAndEstadoOrderByFechaCompletadaDesc(
                eq(COCINERA_ID), eq(EstadoReserva.COMPLETADA), any(PageRequest.class)))
                .thenReturn(page);
        when(reservaMapper.toDomain(reservaEntity)).thenReturn(reservaDominio);

        CuentaEntity cuentaEntity = CuentaEntity.builder().id(1L).build();
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaEntity));
        when(cuentaMapper.toDomain(cuentaEntity))
                .thenReturn(Cuenta.builder().id(1L)
                        .identidad(new Identidad("María", "m@test.com", "300", null)).build());

        PlatoEntity platoEntity = PlatoEntity.builder().id(platoId).nombre("Ajiaco").build();
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(platoEntity));
        when(platoMapper.toDomain(platoEntity)).thenReturn(Plato.builder().id(platoId).nombre("Ajiaco").build());

        HistorialPaginadoResponseDTO result = service.obtenerHistorial(COCINERA_ID, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContenido().size());
        assertEquals("María", result.getContenido().get(0).getComprador());
        assertEquals("Ajiaco", result.getContenido().get(0).getPlato());
        assertEquals(5, result.getContenido().get(0).getCalificacion());
    }

    @Test
    @DisplayName("Pagina negativa → IllegalArgumentException")
    void obtenerHistorial_paginaNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> service.obtenerHistorial(COCINERA_ID, -1, 10));
    }

    @Test
    @DisplayName("Tamaño inválido → IllegalArgumentException")
    void obtenerHistorial_tamanioInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> service.obtenerHistorial(COCINERA_ID, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> service.obtenerHistorial(COCINERA_ID, 0, 100));
    }
}