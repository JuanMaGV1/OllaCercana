package com.ollacercana.persistence.repository;

import com.ollacercana.persistence.entities.DecisionModeracionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DecisionModeracionRepository extends JpaRepository<DecisionModeracionEntity, UUID> {
}