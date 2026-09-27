package com.ollacercana.repository;

import com.ollacercana.domain.CodigoOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodigoOTPRepository extends JpaRepository<CodigoOTP, Long> {

    Optional<CodigoOTP> findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(UUID perfilId);
}