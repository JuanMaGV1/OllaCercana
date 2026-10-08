package com.ollacercana.repository;

import com.ollacercana.domain.EstadoReporte;
import com.ollacercana.domain.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReporteRepository extends JpaRepository<Reporte, UUID> {

                                                                                                         
    boolean existsByReservaIdAndEstado(UUID reservaId, EstadoReporte estado);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT r.reportanteId) FROM Reporte r WHERE r.objetivoId = :objetivoId AND r.objetivo = :objetivo")
    long countDistinctReportanteIdByObjetivoIdAndObjetivo(@org.springframework.data.repository.query.Param("objetivoId") UUID objetivoId, @org.springframework.data.repository.query.Param("objetivo") com.ollacercana.domain.ObjetivoReporte objetivo);

    boolean existsByReportanteIdAndObjetivoIdAndObjetivo(Long reportanteId, UUID objetivoId, com.ollacercana.domain.ObjetivoReporte objetivo);

    boolean existsByReportanteIdAndCuentaObjetivoIdAndObjetivo(Long reportanteId, Long cuentaObjetivoId, com.ollacercana.domain.ObjetivoReporte objetivo);

    org.springframework.data.domain.Page<Reporte> findByEstado(EstadoReporte estado, org.springframework.data.domain.Pageable pageable);
}
