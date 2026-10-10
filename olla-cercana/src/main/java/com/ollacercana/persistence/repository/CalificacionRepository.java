package com.ollacercana.persistence.repository;

import com.ollacercana.core.models.enums.EstadoCalificacion;
import com.ollacercana.persistence.entities.CalificacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

/**
 * OC-193: consultas de calificaciones por reserva y por cocinera.
 */
@Repository
public interface CalificacionRepository extends JpaRepository<CalificacionEntity, UUID> {

    Optional<CalificacionEntity> findByReservaId(UUID reservaId);

    Page<CalificacionEntity> findByCocineraId(UUID cocineraId, EstadoCalificacion estado, Pageable pageable);

    Page<CalificacionEntity> findByCocineraIdAndEstrellas(UUID cocineraId, EstadoCalificacion estado, Integer estrellas, Pageable pageable);

    Page<CalificacionEntity> findByCocineraIdAndEstado(
        UUID cocineraId, EstadoCalificacion estado, Pageable pageable);

    Page<CalificacionEntity> findByCocineraIdAndEstadoAndEstrellas(
            UUID cocineraId, EstadoCalificacion estado, Integer estrellas, Pageable pageable);

    @Query("SELECT AVG(c.estrellas) FROM CalificacionEntity c " +
       "WHERE c.cocineraId = :cocineraId AND c.estado = 'PUBLICADA'")
    Double promedioPorCocinera(@Param("cocineraId") UUID cocineraId);

    @Query("SELECT COUNT(c) FROM CalificacionEntity c " +
        "WHERE c.cocineraId = :cocineraId AND c.estado = 'PUBLICADA' AND c.estrellas >= 4")
    Long contarPositivas(@Param("cocineraId") UUID cocineraId);

    @Query("SELECT COUNT(c) FROM CalificacionEntity c " +
        "WHERE c.cocineraId = :cocineraId AND c.estado = 'PUBLICADA'")
    Long contarTotal(@Param("cocineraId") UUID cocineraId);

    List<CalificacionEntity> findByEstadoAndFechaLimitePublicacionLessThanEqual(
        EstadoCalificacion estado, LocalDateTime ahora);

        /**
 * OC-195 / RN-20: busca la calificación de la contraparte en la misma reserva.
 * Si existe, la actual se puede publicar sin esperar la ventana de 72h.
 */
Optional<CalificacionEntity> findByReservaIdAndCompradorIdNot(UUID reservaId, Long compradorId);
}