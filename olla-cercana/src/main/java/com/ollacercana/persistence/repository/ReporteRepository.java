package com.ollacercana.persistence.repository;

import com.ollacercana.core.models.enums.EstadoReporte;
import com.ollacercana.core.models.enums.ObjetivoReporte;
import com.ollacercana.persistence.entities.ReporteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ReporteRepository extends JpaRepository<ReporteEntity, UUID> {

    boolean existsByReservaIdAndEstado(UUID reservaId, EstadoReporte estado);

    @Query("SELECT COUNT(DISTINCT r.reportanteId) FROM ReporteEntity r " +
            "WHERE r.objetivoId = :objetivoId AND r.objetivo = :objetivo")
    long countDistinctReportanteIdByObjetivoIdAndObjetivo(@Param("objetivoId") UUID objetivoId,
                                                          @Param("objetivo") ObjetivoReporte objetivo);

    boolean existsByReportanteIdAndObjetivoIdAndObjetivo(Long reportanteId, UUID objetivoId, ObjetivoReporte objetivo);

    boolean existsByReportanteIdAndCuentaObjetivoIdAndObjetivo(Long reportanteId, Long cuentaObjetivoId, ObjetivoReporte objetivo);

    Page<ReporteEntity> findByEstado(EstadoReporte estado, Pageable pageable);
}