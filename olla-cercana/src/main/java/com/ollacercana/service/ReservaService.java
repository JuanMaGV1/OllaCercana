package com.ollacercana.service;

import com.ollacercana.model.dto.response.ReservaResponseDTO;
import com.ollacercana.model.domain.Reserva;
import com.ollacercana.model.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.model.dto.request.ReservaRequestDTO;

import java.util.List;
import java.util.UUID;

public interface ReservaService {

    ReservaResponseDTO crear(Long compradorId, ReservaRequestDTO request);

    Reserva obtenerPorId(UUID reservaId);

    List<Reserva> listarPendientesDeCocinera(UUID cocineraId);

    Reserva decidir(UUID reservaId, UUID cocineraId, DecisionReservaRequestDTO request);

    Reserva completar(UUID reservaId, String comentario);

    List<UUID> buscarReservasVencidas();

    Reserva expirar(UUID reservaId);

    List<UUID> buscarReservasParaRecordatorio();

    void enviarRecordatorio(UUID reservaId);

    List<UUID> buscarReservasParaCierreAutomatico();

    Reserva completarAutomaticamente(UUID reservaId);
}