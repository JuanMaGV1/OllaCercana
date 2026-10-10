package com.ollacercana.core.services.impl;

import com.ollacercana.controller.dtos.response.HistorialPaginadoResponseDTO;
import com.ollacercana.controller.dtos.response.HistorialPedidoResponseDTO;
import com.ollacercana.controller.dtos.response.MetricasCocineraResponseDTO;
import com.ollacercana.controller.handlers.exception.ReglaDeNegocioException;
import com.ollacercana.core.models.Cuenta;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.Reserva;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.services.MetricasCocineraService;
import com.ollacercana.persistence.mappers.CuentaEntityMapper;
import com.ollacercana.persistence.mappers.PlatoEntityMapper;
import com.ollacercana.persistence.mappers.ReservaEntityMapper;
import com.ollacercana.persistence.repository.CuentaRepository;
import com.ollacercana.persistence.repository.PlatoPedidosProjection;
import com.ollacercana.persistence.repository.PlatoRepository;
import com.ollacercana.persistence.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * HU-20 — Métricas e historial de ventas de la cocinera.
 * 
 * Retorna ingresos referenciales, porciones entregadas y plato más pedido.
 * Respeta el escenario "sin ventas": ceros y plato vacío.
 *
 * @see OC-275 Maqueta pestaña Historial (front)
 * @see OC-276 Endpoint GET /cocineras/metricas
 * @see OC-277 Consulta de ingresos por periodo
 * @see OC-295/296 Maquetas front
 * @see OC-297 Endpoint del historial de pedidos
 * @see OC-298 Pruebas unitarias de métricas e historial
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetricasCocineraServiceImpl implements MetricasCocineraService {

    public static final int TAMANIO_MAXIMO = 50;
    public static final String COMPRADOR_DESCONOCIDO = "Comprador";

    private static final LocalDateTime INICIO_ACUMULADO = LocalDateTime.of(1970, 1, 1, 0, 0);
    private static final LocalDateTime FIN_ACUMULADO = LocalDateTime.of(2999, 12, 31, 23, 59, 59);

    private final ReservaRepository reservaRepository;
    private final PlatoRepository platoRepository;
    private final CuentaRepository cuentaRepository;
    private final ReservaEntityMapper reservaMapper;
    private final PlatoEntityMapper platoMapper;
    private final CuentaEntityMapper cuentaMapper;

    @Override
    public MetricasCocineraResponseDTO obtenerMetricas(UUID cocineraId, LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaDeNegocioException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }
        LocalDateTime inicio = desde != null ? desde.atStartOfDay() : INICIO_ACUMULADO;
        LocalDateTime fin = hasta != null ? hasta.atTime(23, 59, 59) : FIN_ACUMULADO;
        EstadoReserva estado = EstadoReserva.COMPLETADA;

        BigDecimal ingresos = reservaRepository.sumarIngresosPorPeriodo(cocineraId, estado, inicio, fin);
        Long porciones = reservaRepository.sumarPorcionesPorPeriodo(cocineraId, estado, inicio, fin);

        List<PlatoPedidosProjection> top =
                reservaRepository.findPlatosMasPedidos(cocineraId, estado, inicio, fin, PageRequest.of(0, 1));
        String platoMasPedido = top.isEmpty()
                ? ""
                : platoRepository.findById(top.get(0).getPlatoId())
                        .map(platoMapper::toDomain)
                        .map(Plato::getNombre)
                        .orElse("");

        return MetricasCocineraResponseDTO.builder()
                .ingresosReferenciales(ingresos != null ? ingresos : BigDecimal.ZERO)
                .porcionesEntregadas(porciones != null ? porciones : 0L)
                .platoMasPedido(platoMasPedido)
                .build();
    }

    @Override
    public HistorialPaginadoResponseDTO obtenerHistorial(UUID cocineraId, int pagina, int tamanio) {
        if (pagina < 0) throw new IllegalArgumentException("La página no puede ser negativa");
        if (tamanio < 1 || tamanio > TAMANIO_MAXIMO) {
            throw new IllegalArgumentException("El tamaño debe estar entre 1 y " + TAMANIO_MAXIMO);
        }

        Page<com.ollacercana.persistence.entities.ReservaEntity> resultado =
                reservaRepository.findByCocineraIdAndEstadoOrderByFechaCompletadaDesc(
                        cocineraId, EstadoReserva.COMPLETADA, PageRequest.of(pagina, tamanio));

        List<HistorialPedidoResponseDTO> contenido = resultado.getContent().stream()
                .map(reservaMapper::toDomain)
                .map(this::toHistorial)
                .toList();

        return HistorialPaginadoResponseDTO.builder()
                .contenido(contenido)
                .pagina(resultado.getNumber())
                .tamanio(resultado.getSize())
                .totalElementos(resultado.getTotalElements())
                .totalPaginas(resultado.getTotalPages())
                .build();
    }

    private HistorialPedidoResponseDTO toHistorial(Reserva reserva) {
        String comprador = cuentaRepository.findById(reserva.getCompradorId())
                .map(cuentaMapper::toDomain)
                .map(Cuenta::getIdentidad)
                .map(identidad -> identidad.getNombre())
                .orElse(COMPRADOR_DESCONOCIDO);
        String plato = platoRepository.findById(reserva.getPlatoId())
                .map(platoMapper::toDomain)
                .map(Plato::getNombre)
                .orElse("");

        return HistorialPedidoResponseDTO.builder()
                .reservaId(reserva.getId())
                .plato(plato)
                .fecha(reserva.getFechaCompletada())
                .comprador(comprador)
                .porciones(reserva.getCantidadPorciones())
                .calificacion(reserva.getCalificacion())
                .build();
    }
}
