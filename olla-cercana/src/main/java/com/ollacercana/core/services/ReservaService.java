package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.request.DecisionReservaRequestDTO;
import com.ollacercana.core.models.Reserva;
import com.ollacercana.core.models.enums.MotivoRechazo;

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
    
    List<UUID> buscarReservasParaRecordatorioRecogida(LocalDateTime ahora);

    void marcarRecordatorioRecogidaEnviado(UUID reservaId);
}