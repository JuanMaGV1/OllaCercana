package com.ollacercana.repository;

import com.ollacercana.domain.EstadoReporte;
import com.ollacercana.domain.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReporteRepository extends JpaRepository<Reporte, UUID> {

    /** OC-158: ¿la reserva tiene un reporte en ese estado? (HU-23 bloquea el cierre si está ABIERTO). */
    boolean existsByReservaIdAndEstado(UUID reservaId, EstadoReporte estado);
}
