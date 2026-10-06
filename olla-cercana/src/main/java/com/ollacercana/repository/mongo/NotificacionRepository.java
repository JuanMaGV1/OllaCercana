package com.ollacercana.repository.mongo;

import com.ollacercana.domain.Notificacion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends MongoRepository<Notificacion, String> {
    List<Notificacion> findByReservaIdOrderByFechaCreacionAsc(UUID reservaId);
    List<Notificacion> findByCompradorIdAndLeidaFalse(Long compradorId);
    List<Notificacion> findByCocineraIdAndLeidaFalse(UUID cocineraId);
}