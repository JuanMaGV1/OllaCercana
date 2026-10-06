package com.ollacercana.repository;

import com.ollacercana.model.domain.EstadoReserva;
import com.ollacercana.persistence.entity.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
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
}