package com.ollacercana.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ollacercana.model.domain.EstadoPlato;
import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.domain.TipoComida;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PlatoRepository extends JpaRepository<Plato, UUID> {

    List<Plato> findByEstado(EstadoPlato estado);

    List<Plato> findByTipoComida(TipoComida tipo);

    /**
     * Platos de una cocinera especifica en un estado dado para la RN-28
     * y para "mis platos" en el perfil.
     */
    List<Plato> findByCocineraIdAndEstado(UUID cocineraId, EstadoPlato estado);

    /**
     * RN-02: platos activos y vigentes (fecha de expiración > ahora).
     * RN-03: con porciones disponibles > 0.
     */
    @Query("SELECT p FROM Plato p WHERE p.estado = :estado AND p.fechaExpiracion > :ahora " +
            "AND (p.porcionesTotales - COALESCE(p.porcionesComprometidas, 0)) > 0")
    List<Plato> findActivosVigentes(@Param("estado") EstadoPlato estado, @Param("ahora") LocalDateTime ahora);

    /**
     * Cuenta platos activos vigentes en todo el sistema.
     */
    long countByEstadoAndFechaExpiracionAfter(EstadoPlato estado, LocalDateTime ahora);

    /**
     * Cuenta los platos activos vigentes de una cocinera específica (RN-28).
     */
    long countByCocineraIdAndEstadoAndFechaExpiracionAfter(UUID cocineraId, EstadoPlato estado, LocalDateTime ahora);

    /**
     * Tarea programada: platos que deben expirar.
     */
    List<Plato> findByEstadoInAndFechaExpiracionLessThanEqual(List<EstadoPlato> estados, LocalDateTime ahora);
}