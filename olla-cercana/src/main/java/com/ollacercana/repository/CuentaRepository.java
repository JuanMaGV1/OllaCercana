package com.ollacercana.repository;

import com.ollacercana.persistence.entity.CuentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CuentaRepository extends JpaRepository<CuentaEntity, UUID> {

    boolean existsByCorreo(String correo);

    boolean existsByCelular(String celular);

    Optional<CuentaEntity> findByCorreo(String correo);

    Optional<CuentaEntity> findByCorreoOrCelular(String correo, String celular);
}