package com.ollacercana.persistence.repository.mongo;

import com.ollacercana.persistence.document.EventoReservaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventoReservaRepository extends MongoRepository<EventoReservaDocument, String> {
    
    List<EventoReservaDocument> findByReservaId(UUID reservaId);
}