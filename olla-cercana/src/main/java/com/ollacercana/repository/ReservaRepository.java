package com.ollacercana.repository;

import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    List<Reserva> findByCompradorId(Long compradorId);

    List<Reserva> findByEstadoAndFechaLimiteConfirmacionBefore(EstadoReserva estado, LocalDateTime fecha);

    long countByCompradorIdAndEstado(Long compradorId, EstadoReserva estado);
}