package com.ollacercana.repository;

import com.ollacercana.model.domain.EstadoReporte;
import com.ollacercana.persistence.entity.ReporteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReporteRepository extends JpaRepository<ReporteEntity, UUID> {
    boolean existsByReservaIdAndEstado(UUID reservaId, EstadoReporte estado);
}