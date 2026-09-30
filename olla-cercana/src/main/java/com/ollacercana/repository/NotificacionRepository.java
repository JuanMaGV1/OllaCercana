package com.ollacercana.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ollacercana.model.domain.Notificacion;

import java.util.List;
import java.util.UUID;

public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {

    List<Notificacion> findByReservaIdOrderByFechaCreacionAsc(UUID reservaId);
}
