package com.ollacercana.repository;

import com.ollacercana.domain.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {

    List<Notificacion> findByReservaIdOrderByFechaCreacionAsc(UUID reservaId);
}
