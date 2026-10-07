package com.ollacercana.service;

import com.ollacercana.domain.Cuenta;
import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.Identidad;
import com.ollacercana.domain.Plato;
import com.ollacercana.domain.Reserva;
import com.ollacercana.dto.response.HistorialPaginadoResponseDTO;
import com.ollacercana.dto.response.MetricasCocineraResponseDTO;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.repository.PlatoPedidosProjection;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReservaRepository;
import com.ollacercana.service.impl.MetricasCocineraServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricasCocineraServiceImplTest {

    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private PlatoRepository platoRepository;
    @Mock
    private CuentaRepository cuentaRepository;

    private MetricasCocineraServiceImpl service;

    private final UUID cocineraId = UUID.randomUUID();
    private final UUID otraCocineraId = UUID.randomUUID();
    private final UUID platoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new MetricasCocineraServiceImpl(reservaRepository, platoRepository, cuentaRepository);
    }

    private PlatoPedidosProjection proyeccion(UUID id, long pedidos, long porciones) {
        return new PlatoPedidosProjection() {
            @Override public UUID getPlatoId() { return id; }
            @Override public Long getTotalPedidos() { return pedidos; }
            @Override public Long getTotalPorciones() { return porciones; }
        };
    }

    private Reserva reservaCompletada(UUID cocinera, Long compradorId, int porciones, Integer calificacion) {
        return Reserva.builder()
                .id(UUID.randomUUID())
                .platoId(platoId)
                .cocineraId(cocinera)
                .compradorId(compradorId)
                .cantidadPorciones(porciones)
                .montoTotal(new BigDecimal("15000").multiply(BigDecimal.valueOf(porciones)))
                .estado(EstadoReserva.COMPLETADA)
                .fechaCompletada(LocalDateTime.of(2026, 10, 1, 12, 0))
                .calificacion(calificacion)
                .build();
    }

    @Test
    @DisplayName("Cocinera con ventas: retorna ingresos, porciones y plato más pedido")
    void cocineraConVentas() {
        when(reservaRepository.sumarIngresosPorPeriodo(eq(cocineraId), eq(EstadoReserva.COMPLETADA), any(), any()))
                .thenReturn(new BigDecimal("120000.00"));
        when(reservaRepository.sumarPorcionesPorPeriodo(eq(cocineraId), eq(EstadoReserva.COMPLETADA), any(), any()))
                .thenReturn(8L);
        when(reservaRepository.findPlatosMasPedidos(eq(cocineraId), eq(EstadoReserva.COMPLETADA), any(), any(), any(Pageable.class)))
                .thenReturn(List.of(proyeccion(platoId, 5, 8)));
        when(platoRepository.findById(platoId))
                .thenReturn(Optional.of(Plato.builder().id(platoId).nombre("Sancocho de Pollo").build()));

        MetricasCocineraResponseDTO metricas = service.obtenerMetricas(cocineraId, null, null);

        assertEquals(new BigDecimal("120000.00"), metricas.getIngresosReferenciales());
        assertEquals(8L, metricas.getPorcionesEntregadas());
        assertEquals("Sancocho de Pollo", metricas.getPlatoMasPedido());
    }

    @Test
    @DisplayName("Cocinera sin ventas: devuelve ceros y plato más pedido vacío")
    void cocineraSinVentas() {
        when(reservaRepository.sumarIngresosPorPeriodo(eq(cocineraId), any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(reservaRepository.sumarPorcionesPorPeriodo(eq(cocineraId), any(), any(), any())).thenReturn(0L);
        when(reservaRepository.findPlatosMasPedidos(eq(cocineraId), any(), any(), any(), any(Pageable.class)))
                .thenReturn(List.of());

        MetricasCocineraResponseDTO metricas = service.obtenerMetricas(cocineraId, null, null);

        assertEquals(0, BigDecimal.ZERO.compareTo(metricas.getIngresosReferenciales()));
        assertEquals(0L, metricas.getPorcionesEntregadas());
        assertEquals("", metricas.getPlatoMasPedido());
    }

    @Test
    @DisplayName("Plato más pedido: toma el primero del ranking por número de pedidos")
    void platoMasPedidoCorrecto() {
        UUID otroPlato = UUID.randomUUID();
        when(reservaRepository.sumarIngresosPorPeriodo(any(), any(), any(), any())).thenReturn(new BigDecimal("30000"));
        when(reservaRepository.sumarPorcionesPorPeriodo(any(), any(), any(), any())).thenReturn(2L);
        when(reservaRepository.findPlatosMasPedidos(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(List.of(proyeccion(otroPlato, 3, 3), proyeccion(platoId, 1, 1)));
        when(platoRepository.findById(otroPlato))
                .thenReturn(Optional.of(Plato.builder().id(otroPlato).nombre("Ajiaco santafereño").build()));

        MetricasCocineraResponseDTO metricas = service.obtenerMetricas(cocineraId, null, null);

        assertEquals("Ajiaco santafereño", metricas.getPlatoMasPedido());
    }

    @Test
    @DisplayName("Periodo: convierte las fechas al inicio y fin del día")
    void periodoSeConvierteARango() {
        when(reservaRepository.sumarIngresosPorPeriodo(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(reservaRepository.sumarPorcionesPorPeriodo(any(), any(), any(), any())).thenReturn(0L);
        when(reservaRepository.findPlatosMasPedidos(any(), any(), any(), any(), any(Pageable.class))).thenReturn(List.of());

        service.obtenerMetricas(cocineraId, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5));

        verify(reservaRepository).sumarIngresosPorPeriodo(cocineraId, EstadoReserva.COMPLETADA,
                LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 10, 5, 23, 59, 59));
    }

    @Test
    @DisplayName("Periodo inválido: desde posterior a hasta lanza regla de negocio")
    void periodoInvalido() {
        assertThrows(ReglaDeNegocioException.class,
                () -> service.obtenerMetricas(cocineraId, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 1)));
    }

    @Test
    @DisplayName("Historial: solo consulta pedidos de la cocinera autenticada, no de otra")
    void historialSoloDeLaCocinera() {
        Reserva propia = reservaCompletada(cocineraId, 7L, 2, 5);
        when(reservaRepository.findByCocineraIdAndEstadoOrderByFechaCompletadaDesc(
                eq(cocineraId), eq(EstadoReserva.COMPLETADA), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(propia), PageRequest.of(0, 10), 1));
        when(cuentaRepository.findById(7L)).thenReturn(Optional.of(
                Cuenta.builder().id(7L).identidad(Identidad.builder().nombre("Laura Gómez").build()).build()));
        when(platoRepository.findById(platoId))
                .thenReturn(Optional.of(Plato.builder().id(platoId).nombre("Sancocho de Pollo").build()));

        HistorialPaginadoResponseDTO historial = service.obtenerHistorial(cocineraId, 0, 10);

        assertEquals(1, historial.getContenido().size());
        assertEquals("Laura Gómez", historial.getContenido().get(0).getComprador());
        assertEquals(2, historial.getContenido().get(0).getPorciones());
        assertEquals(5, historial.getContenido().get(0).getCalificacion());
        verify(reservaRepository).findByCocineraIdAndEstadoOrderByFechaCompletadaDesc(
                eq(cocineraId), eq(EstadoReserva.COMPLETADA), any(Pageable.class));
        assertNotEquals(cocineraId, otraCocineraId);
    }

    @Test
    @DisplayName("Historial: pedido sin calificación devuelve calificación nula")
    void pedidoSinCalificacion() {
        Reserva sinCalificar = reservaCompletada(cocineraId, 9L, 1, null);
        when(reservaRepository.findByCocineraIdAndEstadoOrderByFechaCompletadaDesc(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sinCalificar), PageRequest.of(0, 10), 1));
        when(cuentaRepository.findById(9L)).thenReturn(Optional.empty());
        when(platoRepository.findById(platoId)).thenReturn(Optional.empty());

        HistorialPaginadoResponseDTO historial = service.obtenerHistorial(cocineraId, 0, 10);

        assertNull(historial.getContenido().get(0).getCalificacion());
        assertEquals(MetricasCocineraServiceImpl.COMPRADOR_DESCONOCIDO, historial.getContenido().get(0).getComprador());
    }

    @Test
    @DisplayName("Historial vacío: cocinera nueva recibe una página sin contenido")
    void historialVacio() {
        when(reservaRepository.findByCocineraIdAndEstadoOrderByFechaCompletadaDesc(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        HistorialPaginadoResponseDTO historial = service.obtenerHistorial(cocineraId, 0, 10);

        assertTrue(historial.getContenido().isEmpty());
        assertEquals(0, historial.getTotalElementos());
    }

    @Test
    @DisplayName("Historial: paginación inválida lanza IllegalArgumentException")
    void paginacionInvalida() {
        assertThrows(IllegalArgumentException.class, () -> service.obtenerHistorial(cocineraId, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> service.obtenerHistorial(cocineraId, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> service.obtenerHistorial(cocineraId, 0, 500));
    }
}
