package com.ollacercana.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ollacercana.model.domain.EventoReserva;

import java.util.UUID;

@Repository
public interface EventoReservaRepository extends JpaRepository<EventoReserva, UUID> {
}