package com.ollacercana.persistence.repository;

import com.ollacercana.persistence.entities.CalificacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * OC-193: consultas de calificaciones por reserva y por cocinera.
 */
@Repository
public interface CalificacionRepository extends JpaRepository<CalificacionEntity, UUID> {

    Optional<CalificacionEntity> findByReservaId(UUID reservaId);

    Page<CalificacionEntity> findByCocineraId(UUID cocineraId, Pageable pageable);

    Page<CalificacionEntity> findByCocineraIdAndEstrellas(UUID cocineraId, Integer estrellas, Pageable pageable);

    @Query("SELECT AVG(c.estrellas) FROM CalificacionEntity c WHERE c.cocineraId = :cocineraId")
    Double promedioPorCocinera(@Param("cocineraId") UUID cocineraId);

    @Query("SELECT COUNT(c) FROM CalificacionEntity c " +
            "WHERE c.cocineraId = :cocineraId AND c.estrellas >= 4")
    Long contarPositivas(@Param("cocineraId") UUID cocineraId);

    @Query("SELECT COUNT(c) FROM CalificacionEntity c WHERE c.cocineraId = :cocineraId")
    Long contarTotal(@Param("cocineraId") UUID cocineraId);
}