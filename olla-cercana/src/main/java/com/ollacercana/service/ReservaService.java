package com.ollacercana.service;

import com.ollacercana.model.domain.MotivoRechazo;
import com.ollacercana.model.domain.Reserva;
import com.ollacercana.model.dto.request.DecisionReservaRequestDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservaService extends IReservaService {

    Reserva obtenerPorId(UUID reservaId);

    List<Reserva> listarPendientesDeCocinera(UUID cocineraId);

    Reserva decidir(UUID reservaId, UUID cocineraId, DecisionReservaRequestDTO request);

    Reserva confirmar(UUID reservaId, LocalDateTime horaEstimada);

    Reserva rechazar(UUID reservaId, MotivoRechazo motivo, String comentario);

    List<UUID> buscarReservasVencidas();

    Reserva expirar(UUID reservaId);

    List<UUID> buscarReservasParaRecordatorio();

    void enviarRecordatorio(UUID reservaId);

    Reserva completar(UUID reservaId, String comentario);

    List<UUID> buscarReservasParaCierreAutomatico();

    Reserva completarAutomaticamente(UUID reservaId);
}