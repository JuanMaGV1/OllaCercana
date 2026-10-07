package com.ollacercana.repository;

import com.ollacercana.domain.DecisionModeracion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DecisionModeracionRepository extends JpaRepository<DecisionModeracion, UUID> {
}
