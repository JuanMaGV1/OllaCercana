package com.ollacercana.repository;

import com.ollacercana.domain.EventoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventoReservaRepository extends JpaRepository<EventoReserva, UUID> {
}