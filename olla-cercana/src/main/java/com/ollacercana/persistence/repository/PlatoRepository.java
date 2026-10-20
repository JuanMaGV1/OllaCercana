package com.ollacercana.persistence.repository;

import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.TipoComida;
import com.ollacercana.persistence.entities.PlatoEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
        SELECT p,
            (6371000 * acos(
                cos(radians(:lat)) * cos(radians(p.latitud)) *
                cos(radians(p.longitud) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(p.latitud))
            )) AS distancia
        FROM PlatoEntity p
        WHERE p.estado = :estado
        AND (p.porcionesTotales - p.porcionesComprometidas) > 0
        AND (:tipoComida IS NULL OR p.tipoComida = :tipoComida)
        AND p.latitud IS NOT NULL
        AND p.longitud IS NOT NULL
        AND (6371000 * acos(
                cos(radians(:lat)) * cos(radians(p.latitud)) *
                cos(radians(p.longitud) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(p.latitud))
            )) <= :radioMetros
        ORDER BY distancia ASC
        """)
    Page<Object[]> buscarCercanosConDistancia(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radioMetros") Integer radioMetros,
            @Param("tipoComida") TipoComida tipoComida,
            @Param("estado") EstadoPlato estado,
            Pageable pageable
    );

    long countByEstadoAndFechaExpiracionAfter(EstadoPlato estado, LocalDateTime ahora);
    long countByCocineraIdAndEstadoAndFechaExpiracionAfter(UUID cocineraId, EstadoPlato estado, LocalDateTime ahora);
    List<PlatoEntity> findByEstadoInAndFechaExpiracionLessThanEqual(List<EstadoPlato> estados, LocalDateTime ahora);

    @Query(value = "SELECT p.conjunto_residencial AS conjunto, " +
        "SUM(pl.porciones_totales) AS publicadas, " +
        "SUM(pl.porciones_comprometidas) AS vendidas " +
        "FROM platos pl " +
        "JOIN perfiles_cocinera p ON p.id = pl.cocinera_id " +
        "WHERE pl.fecha_publicacion >= :desde AND pl.fecha_publicacion < :hasta " +
        "GROUP BY p.conjunto_residencial",
        nativeQuery = true)
List<BalanceConjuntoProjection> balancePorConjunto(@Param("desde") LocalDateTime desde,
                                                   @Param("hasta") LocalDateTime hasta);

    @Query("""
        SELECT p FROM PlatoEntity p
        WHERE p.estado = :estado
        AND p.fechaExpiracion > :ahora
        AND (p.porcionesTotales - COALESCE(p.porcionesComprometidas, 0)) > 0
        AND p.latitud IS NOT NULL
        AND p.longitud IS NOT NULL
        """)
    List<PlatoEntity> findOfertasActivasConUbicacion(
            @Param("estado") EstadoPlato estado,
            @Param("ahora") LocalDateTime ahora
    );
}
