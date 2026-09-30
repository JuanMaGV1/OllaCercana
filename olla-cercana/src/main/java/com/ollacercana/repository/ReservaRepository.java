package com.ollacercana.repository;

import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    /**
     * HU-12: solicitudes de una cocinera en un estado, la más urgente primero.
     */
    List<Reserva> findByCocineraIdAndEstadoOrderByFechaLimiteConfirmacionAsc(UUID cocineraId, EstadoReserva estado);

    /**
     * RN-04 / OC-148: reservas cuya hora límite de confirmación ya pasó.
     */
    List<Reserva> findByEstadoAndFechaLimiteConfirmacionLessThanEqual(EstadoReserva estado, LocalDateTime ahora);

    /**
     * HU-23 / OC-157: reservas en ese estado cuya decisión (confirmación) ocurrió hasta el límite dado.
     */
    List<Reserva> findByEstadoAndFechaDecisionLessThanEqual(EstadoReserva estado, LocalDateTime limite);

    /**
     * RN-25 / OC-149: reservas creadas hace 7 minutos o más, aún vigentes y sin recordatorio.
     */
    @Query("SELECT r FROM Reserva r WHERE r.estado = :estado AND r.recordatorioEnviado = false " +
            "AND r.fechaCreacion <= :creadaAntesDe AND r.fechaLimiteConfirmacion > :ahora")
    List<Reserva> findPendientesParaRecordatorio(@Param("estado") EstadoReserva estado,
                                                 @Param("creadaAntesDe") LocalDateTime creadaAntesDe,
                                                 @Param("ahora") LocalDateTime ahora);
}
