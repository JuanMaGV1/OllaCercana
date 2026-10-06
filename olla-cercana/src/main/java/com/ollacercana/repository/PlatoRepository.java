package com.ollacercana.repository;

import com.ollacercana.model.domain.EstadoPlato;
import com.ollacercana.model.domain.TipoComida;
import com.ollacercana.persistence.entity.PlatoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PlatoRepository extends JpaRepository<PlatoEntity, UUID> {

    List<PlatoEntity> findByEstado(EstadoPlato estado);
    List<PlatoEntity> findByTipoComida(TipoComida tipo);
    List<PlatoEntity> findByCocineraIdAndEstado(UUID cocineraId, EstadoPlato estado);

    @Query("SELECT p FROM PlatoEntity p WHERE p.estado = :estado AND p.fechaExpiracion > :ahora " +
           "AND (p.porcionesTotales - COALESCE(p.porcionesComprometidas, 0)) > 0")
    List<PlatoEntity> findActivosVigentes(@Param("estado") EstadoPlato estado,
                                          @Param("ahora") LocalDateTime ahora);

    long countByEstadoAndFechaExpiracionAfter(EstadoPlato estado, LocalDateTime ahora);
    long countByCocineraIdAndEstadoAndFechaExpiracionAfter(UUID cocineraId, EstadoPlato estado, LocalDateTime ahora);
    List<PlatoEntity> findByEstadoInAndFechaExpiracionLessThanEqual(List<EstadoPlato> estados, LocalDateTime ahora);
}