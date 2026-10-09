package com.ollacercana.persistence.repository;

import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.persistence.entities.ReservaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservaRepository extends JpaRepository<ReservaEntity, UUID> {

    List<ReservaEntity> findByCompradorId(Long compradorId);
    List<ReservaEntity> findByEstadoAndFechaLimiteConfirmacionBefore(EstadoReserva estado, LocalDateTime fecha);
    long countByCompradorIdAndEstado(Long compradorId, EstadoReserva estado);
    List<ReservaEntity> findByCocineraIdAndEstadoOrderByFechaLimiteConfirmacionAsc(UUID cocineraId, EstadoReserva estado);
    List<ReservaEntity> findByEstadoAndFechaLimiteConfirmacionLessThanEqual(EstadoReserva estado, LocalDateTime ahora);
    List<ReservaEntity> findByEstadoAndFechaDecisionLessThanEqual(EstadoReserva estado, LocalDateTime limite);

    @Query("SELECT r FROM ReservaEntity r WHERE r.estado = :estado AND r.recordatorioEnviado = false " +
            "AND r.fechaCreacion <= :creadaAntesDe AND r.fechaLimiteConfirmacion > :ahora")
    List<ReservaEntity> findPendientesParaRecordatorio(@Param("estado") EstadoReserva estado,
                                                       @Param("creadaAntesDe") LocalDateTime creadaAntesDe,
                                                       @Param("ahora") LocalDateTime ahora);

    // ---- HU-20: métricas ----

    @Query("SELECT COALESCE(SUM(r.montoTotal), 0) FROM ReservaEntity r " +
            "WHERE r.cocineraId = :cocineraId AND r.estado = :estado " +
            "AND r.fechaCompletada >= :desde AND r.fechaCompletada <= :hasta")
    BigDecimal sumarIngresosPorPeriodo(@Param("cocineraId") UUID cocineraId,
                                       @Param("estado") EstadoReserva estado,
                                       @Param("desde") LocalDateTime desde,
                                       @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COALESCE(SUM(r.cantidadPorciones), 0) FROM ReservaEntity r " +
            "WHERE r.cocineraId = :cocineraId AND r.estado = :estado " +
            "AND r.fechaCompletada >= :desde AND r.fechaCompletada <= :hasta")
    Long sumarPorcionesPorPeriodo(@Param("cocineraId") UUID cocineraId,
                                  @Param("estado") EstadoReserva estado,
                                  @Param("desde") LocalDateTime desde,
                                  @Param("hasta") LocalDateTime hasta);

    @Query("SELECT r.platoId AS platoId, COUNT(r) AS totalPedidos, SUM(r.cantidadPorciones) AS totalPorciones " +
            "FROM ReservaEntity r " +
            "WHERE r.cocineraId = :cocineraId AND r.estado = :estado " +
            "AND r.fechaCompletada >= :desde AND r.fechaCompletada <= :hasta " +
            "GROUP BY r.platoId " +
            "ORDER BY COUNT(r) DESC, SUM(r.cantidadPorciones) DESC")
    List<PlatoPedidosProjection> findPlatosMasPedidos(@Param("cocineraId") UUID cocineraId,
                                                      @Param("estado") EstadoReserva estado,
                                                      @Param("desde") LocalDateTime desde,
                                                      @Param("hasta") LocalDateTime hasta,
                                                      Pageable pageable);

    Page<ReservaEntity> findByCocineraIdAndEstadoOrderByFechaCompletadaDesc(UUID cocineraId,
                                                                            EstadoReserva estado,
                                                                            Pageable pageable);

    @Query("SELECT COUNT(r) FROM ReservaEntity r " +
            "WHERE r.compradorId = :compradorId AND r.cocineraId = :cocineraId AND r.estado = :estado " +
            "AND r.fechaCompletada >= :desde AND r.fechaCompletada < :hasta")
    long contarCompletadasEnPeriodo(@Param("compradorId") Long compradorId,
                                    @Param("cocineraId") UUID cocineraId,
                                    @Param("estado") EstadoReserva estado,
                                    @Param("desde") LocalDateTime desde,
                                    @Param("hasta") LocalDateTime hasta);

    // ---- RN-18 / OC-266: Purga de mensajes de reservas cerradas ----
    List<ReservaEntity> findByEstadoAndFechaCompletadaLessThanEqual(EstadoReserva estado, LocalDateTime fecha);
}