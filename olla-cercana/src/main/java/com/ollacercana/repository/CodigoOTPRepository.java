package com.ollacercana.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ollacercana.model.domain.CodigoOTP;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodigoOTPRepository extends JpaRepository<CodigoOTP, Long> {

    Optional<CodigoOTP> findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(UUID perfilId);
}