package com.ollacercana.repository;

import com.ollacercana.domain.PerfilCocinera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerfilCocineraRepository extends JpaRepository<PerfilCocinera, UUID> {

    Optional<PerfilCocinera> findByCuentaId(Long cuentaId);

    List<PerfilCocinera> findByEsDestacadaTrue();

    boolean existsByNumeroNequi(String numeroNequi);

    boolean existsByNumeroDaviplata(String numeroDaviplata);

    boolean existsByNumeroNequiAndIdNot(String numeroNequi, UUID id);

    boolean existsByNumeroDaviplataAndIdNot(String numeroDaviplata, UUID id);

    List<PerfilCocinera> findByPausadaTrue();
}