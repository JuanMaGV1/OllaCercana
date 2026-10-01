package com.ollacercana.repository;

import com.ollacercana.persistence.entity.PerfilCocineraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerfilCocineraRepository extends JpaRepository<PerfilCocineraEntity, UUID> {

    Optional<PerfilCocineraEntity> findByCuentaId(UUID cuentaId);

    List<PerfilCocineraEntity> findByEsDestacadaTrue();

    boolean existsByNumeroNequi(String numeroNequi);

    boolean existsByNumeroDaviplata(String numeroDaviplata);
}