package com.ollacercana.service;

import com.ollacercana.domain.MotivoRechazo;
import com.ollacercana.domain.Reserva;
import com.ollacercana.dto.request.DecisionReservaRequestDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservaService {

    /**
     * Consulta una reserva por id.
     */
    Reserva obtenerPorId(UUID reservaId);

    /**
     * HU-12: solicitudes PENDIENTES (y aún vigentes) de una cocinera, la más urgente primero.
     */
    List<Reserva> listarPendientesDeCocinera(UUID cocineraId);

    /**
     * HU-12 / OC-150: verifica que la reserva sea de la cocinera y aplica su decisión.
     */
    Reserva decidir(UUID reservaId, UUID cocineraId, DecisionReservaRequestDTO request);

    /**
     * OC-146: pasa la reserva a CONFIRMADA, conserva las porciones descontadas y habilita el chat.
     */
    Reserva confirmar(UUID reservaId, LocalDateTime horaEstimada);

    /**
     * OC-147: pasa la reserva a RECHAZADA y devuelve las porciones al plato (RN-03).
     */
    Reserva rechazar(UUID reservaId, MotivoRechazo motivo, String comentario);

    /**
     * OC-148: ids de las reservas PENDIENTES cuya hora límite ya pasó (RN-04).
     */
    List<UUID> buscarReservasVencidas();

    /**
     * OC-148: marca la reserva como EXPIRADA y devuelve las porciones (RN-04).
     * Si la reserva ya no está pendiente o aún no vence, no hace nada.
     */
    Reserva expirar(UUID reservaId);

    /**
     * OC-149: ids de las reservas que ya cumplen 7 minutos sin respuesta (RN-25).
     */
    List<UUID> buscarReservasParaRecordatorio();

    /**
     * OC-149: envía el recordatorio a la cocinera una sola vez (RN-25).
     * Si la cocinera ya respondió o el recordatorio ya se envió, no hace nada.
     */
    void enviarRecordatorio(UUID reservaId);

    /**
     * HU-23 / OC-156: cierra la transacción. Verifica que la reserva esté CONFIRMADA (422) y que no tenga
     * un reporte ABIERTO (422, OC-158); la pasa a COMPLETADA, deja el chat en SOLO_LECTURA (RN-17, OC-159)
     * y habilita la calificación.
     */
    Reserva completar(UUID reservaId, String comentario);

    /**
     * HU-23 / OC-157: ids de las reservas CONFIRMADAS que llevan 24 horas o más sin cierre.
     */
    List<UUID> buscarReservasParaCierreAutomatico();

    /**
     * HU-23 / OC-157: completa la reserva automáticamente y avisa a ambas partes.
     * Si ya no está confirmada, aún no cumple las 24 horas o tiene un reporte abierto, no hace nada.
     */
    Reserva completarAutomaticamente(UUID reservaId);
}
