package com.ollacercana.repository;

import com.ollacercana.domain.CodigoMedalla;
import com.ollacercana.domain.Medalla;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedallaRepository extends JpaRepository<Medalla, CodigoMedalla> {
}
