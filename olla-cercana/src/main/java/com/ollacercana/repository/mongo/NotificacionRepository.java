package com.ollacercana.repository.mongo;

import com.ollacercana.persistence.document.NotificacionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends MongoRepository<NotificacionDocument, String> {
    List<NotificacionDocument> findByReservaIdOrderByFechaCreacionAsc(UUID reservaId);
    List<NotificacionDocument> findByCompradorIdAndLeidaFalse(Long compradorId);
    List<NotificacionDocument> findByCocineraIdAndLeidaFalse(UUID cocineraId);
}