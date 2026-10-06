package com.ollacercana.repository;

import com.ollacercana.persistence.entity.CodigoOTPEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodigoOTPRepository extends JpaRepository<CodigoOTPEntity, Long> {
    Optional<CodigoOTPEntity> findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(UUID perfilId);
}