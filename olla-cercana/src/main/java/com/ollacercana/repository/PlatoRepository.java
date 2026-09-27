package com.ollacercana.repository;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.Plato;
import com.ollacercana.domain.TipoComida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * OC-90: repositorio JPA de Plato (reemplaza el listado en memoria anterior).
 */
@Repository
public interface PlatoRepository extends JpaRepository<Plato, UUID> {

    // ============ OC-90: requeridos por la subtarea ============

    List<Plato> findByCocineraId(UUID cocineraId);

    List<Plato> findByCocineraIdAndEstado(UUID cocineraId, EstadoPlato estado);

    long countByCocineraIdAndEstado(UUID cocineraId, EstadoPlato estado);

    // ============ Consultas adicionales ya usadas por el módulo ============

    List<Plato> findByEstado(EstadoPlato estado);

    List<Plato> findByTipoComida(TipoComida tipo);

    /**
     * RN-02: platos activos y vigentes (fecha de expiración > ahora).
     * RN-03: con porciones disponibles > 0.
     */
    @Query("""
            SELECT p FROM Plato p
            WHERE p.estado = :estado
              AND p.fechaExpiracion > :ahora
              AND (p.porcionesTotales - COALESCE(p.porcionesComprometidas, 0)) > 0
            """)
    List<Plato> findActivosVigentes(@Param("estado") EstadoPlato estado, @Param("ahora") LocalDateTime ahora);

    @Query("""
            SELECT COUNT(p) FROM Plato p
            WHERE p.estado = :estado
              AND p.fechaExpiracion > :ahora
            """)
    long countActivosVigentes(@Param("estado") EstadoPlato estado, @Param("ahora") LocalDateTime ahora);

    @Query("""
            SELECT COUNT(p) FROM Plato p
            WHERE p.cocineraId = :cocineraId
              AND p.estado = :estado
              AND p.fechaExpiracion > :ahora
            """)
    long countActivosVigentesPorCocinera(@Param("cocineraId") UUID cocineraId,
                                         @Param("estado") EstadoPlato estado,
                                         @Param("ahora") LocalDateTime ahora);

    @Query("""
            SELECT p FROM Plato p
            WHERE p.estado IN :estados
              AND p.fechaExpiracion <= :ahora
            """)
    List<Plato> findParaExpirar(@Param("estados") List<EstadoPlato> estados, @Param("ahora") LocalDateTime ahora);
}
