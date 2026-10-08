package com.ollacercana.persistence.repository;

import com.ollacercana.core.models.enums.CodigoMedalla;
import com.ollacercana.persistence.entities.MedallaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedallaRepository extends JpaRepository<MedallaEntity, CodigoMedalla> {
}
