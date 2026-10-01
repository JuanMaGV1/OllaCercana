package com.ollacercana.repository;

import com.ollacercana.persistence.entity.NotificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificacionRepository extends JpaRepository<NotificacionEntity, UUID> {

    List<NotificacionEntity> findByReservaIdOrderByFechaCreacionAsc(UUID reservaId);
}