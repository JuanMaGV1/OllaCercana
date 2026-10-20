package com.ollacercana.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ollacercana.persistence.entities.CodigoOTPEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodigoOTPRepository extends JpaRepository<CodigoOTPEntity, Long> {
    Optional<CodigoOTPEntity> findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(UUID perfilId);
}