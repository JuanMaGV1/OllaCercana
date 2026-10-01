package com.ollacercana.repository;

import com.ollacercana.model.domain.EstadoReserva;
import com.ollacercana.model.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    long countByCompradorIdAndEstado(Long compradorId, EstadoReserva estado);

    List<Reserva> findByCocineraIdAndEstadoOrderByFechaLimiteConfirmacionAsc(
            UUID cocineraId, EstadoReserva estado);

    List<Reserva> findByEstadoAndFechaLimiteConfirmacionLessThanEqual(
            EstadoReserva estado, LocalDateTime ahora);

    List<Reserva> findByEstadoAndFechaDecisionLessThanEqual(
            EstadoReserva estado, LocalDateTime limite);

    @Query("SELECT r FROM Reserva r WHERE r.estado = :estado " +
           "AND r.recordatorioEnviado = false " +
           "AND r.fechaCreacion <= :creadaAntesDe " +
           "AND r.fechaLimiteConfirmacion > :ahora")
    List<Reserva> findPendientesParaRecordatorio(
            @Param("estado") EstadoReserva estado,
            @Param("creadaAntesDe") LocalDateTime creadaAntesDe,
            @Param("ahora") LocalDateTime ahora);
}