package com.ollacercana.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ollacercana.persistence.entities.PerfilCocineraEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerfilCocineraRepository extends JpaRepository<PerfilCocineraEntity, UUID> {

    Optional<PerfilCocineraEntity> findByCuentaId(Long cuentaId);

    List<PerfilCocineraEntity> findByEsDestacadaTrue();

    List<PerfilCocineraEntity> findByConjuntoResidencial(String conjuntoResidencial);

    boolean existsByNumeroNequi(String numeroNequi);

    boolean existsByNumeroDaviplata(String numeroDaviplata);

    boolean existsByNumeroNequiAndIdNot(String numeroNequi, UUID id);

    boolean existsByNumeroDaviplataAndIdNot(String numeroDaviplata, UUID id);

    List<PerfilCocineraEntity> findByPausadaTrue();
}