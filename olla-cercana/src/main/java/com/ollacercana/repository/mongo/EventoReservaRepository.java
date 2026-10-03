package com.ollacercana.repository.mongo;

import com.ollacercana.domain.EventoReserva;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventoReservaRepository extends MongoRepository<EventoReserva, String> {
    List<EventoReserva> findByReservaId(UUID reservaId);
}