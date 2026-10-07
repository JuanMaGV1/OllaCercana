package com.ollacercana.repository;

import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    List<Reserva> findByCompradorId(Long compradorId);

    List<Reserva> findByEstadoAndFechaLimiteConfirmacionBefore(EstadoReserva estado, LocalDateTime fecha);

    long countByCompradorIdAndEstado(Long compradorId, EstadoReserva estado);

    List<Reserva> findByCocineraIdAndEstadoOrderByFechaLimiteConfirmacionAsc(UUID cocineraId, EstadoReserva estado);

    List<Reserva> findByEstadoAndFechaLimiteConfirmacionLessThanEqual(EstadoReserva estado, LocalDateTime ahora);

    List<Reserva> findByEstadoAndFechaDecisionLessThanEqual(EstadoReserva estado, LocalDateTime limite);

    @Query("SELECT r FROM Reserva r WHERE r.estado = :estado AND r.recordatorioEnviado = false " +
            "AND r.fechaCreacion <= :creadaAntesDe AND r.fechaLimiteConfirmacion > :ahora")
    List<Reserva> findPendientesParaRecordatorio(@Param("estado") EstadoReserva estado,
                                                 @Param("creadaAntesDe") LocalDateTime creadaAntesDe,
                                                 @Param("ahora") LocalDateTime ahora);

    // ---- HU-20: métricas e historial de la cocinera (solo reservas COMPLETADAS) ----

    @Query("SELECT COALESCE(SUM(r.montoTotal), 0) FROM Reserva r " +
            "WHERE r.cocineraId = :cocineraId AND r.estado = :estado " +
            "AND r.fechaCompletada >= :desde AND r.fechaCompletada <= :hasta")
    BigDecimal sumarIngresosPorPeriodo(@Param("cocineraId") UUID cocineraId,
                                       @Param("estado") EstadoReserva estado,
                                       @Param("desde") LocalDateTime desde,
                                       @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COALESCE(SUM(r.cantidadPorciones), 0) FROM Reserva r " +
            "WHERE r.cocineraId = :cocineraId AND r.estado = :estado " +
            "AND r.fechaCompletada >= :desde AND r.fechaCompletada <= :hasta")
    Long sumarPorcionesPorPeriodo(@Param("cocineraId") UUID cocineraId,
                                  @Param("estado") EstadoReserva estado,
                                  @Param("desde") LocalDateTime desde,
                                  @Param("hasta") LocalDateTime hasta);

    @Query("SELECT r.platoId AS platoId, COUNT(r) AS totalPedidos, SUM(r.cantidadPorciones) AS totalPorciones " +
            "FROM Reserva r " +
            "WHERE r.cocineraId = :cocineraId AND r.estado = :estado " +
            "AND r.fechaCompletada >= :desde AND r.fechaCompletada <= :hasta " +
            "GROUP BY r.platoId " +
            "ORDER BY COUNT(r) DESC, SUM(r.cantidadPorciones) DESC")
    List<PlatoPedidosProjection> findPlatosMasPedidos(@Param("cocineraId") UUID cocineraId,
                                                      @Param("estado") EstadoReserva estado,
                                                      @Param("desde") LocalDateTime desde,
                                                      @Param("hasta") LocalDateTime hasta,
                                                      Pageable pageable);

    Page<Reserva> findByCocineraIdAndEstadoOrderByFechaCompletadaDesc(UUID cocineraId,
                                                                      EstadoReserva estado,
                                                                      Pageable pageable);
}
