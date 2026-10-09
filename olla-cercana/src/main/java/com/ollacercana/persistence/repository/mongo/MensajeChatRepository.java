package com.ollacercana.persistence.repository.mongo;

import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.persistence.document.MensajeChatDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MensajeChatRepository extends MongoRepository<MensajeChatDocument, String> {

    List<MensajeChatDocument> findByReservaIdOrderByFechaAsc(UUID reservaId);

    List<MensajeChatDocument> findByReservaIdAndFechaAfterOrderByFechaAsc(UUID reservaId, LocalDateTime fecha);

    List<MensajeChatDocument> findByReservaIdAndLeidoFalse(UUID reservaId);

    long countByReservaIdAndLeidoFalseAndAutorRolNot(UUID reservaId, Rol rol);
}