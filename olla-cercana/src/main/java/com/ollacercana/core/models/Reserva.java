package com.ollacercana.core.models;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.ollacercana.controller.handlers.exception.*;
import com.ollacercana.core.models.enums.EstadoChat;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.models.enums.MedioPago;
import com.ollacercana.core.models.enums.MotivoRechazo;

/**
 * Dominio puro de Reserva — sin anotaciones JPA.
 *
 * FEAT-07 — Reservas (OC-41)
 * HU-11   — Apartar porciones (OC-27)
 * HU-12   — Aceptar / rechazar (OC-28)
 * HU-23   — Confirmar entrega y cerrar transacción (OC-47)
 * RN-04   — 10 minutos para confirmar; si no, expira
 * RN-14   — La cocinera no puede reservar su propio plato
 * RN-15   — Máximo 2 reservas pendientes simultáneas
 * RN-16   — Lock optimista (@Version)
 * RN-17   — Chat habilitado solo tras confirmar
 * RN-25   — Recordatorio a la cocinera a los 7 min
 * RN-32   — Solo se completa si está CONFIRMADA
 * RN-33   — Cierre automático a las 24h
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    public static final int MINUTOS_PARA_CONFIRMAR = 10;
    public static final int MINUTOS_PARA_RECORDATORIO = 7;
    public static final int MAX_CARACTERES_COMENTARIO = 150;
    public static final int HORAS_PARA_CIERRE_AUTOMATICO = 24;

    private UUID id;
    private UUID platoId;
    private UUID cocineraId;
    private Long compradorId;
    private Integer cantidadPorciones;
    private BigDecimal montoTotal;
    private MedioPago medioPago;
    private EstadoReserva estado;
    private String notaComprador;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLimiteConfirmacion;
    private LocalDateTime fechaDecision;
    private LocalDateTime horaEstimadaEntrega;
    private MotivoRechazo motivoRechazo;
    private String comentarioRechazo;
    private boolean recordatorioEnviado;
    private boolean chatHabilitado;
    private EstadoChat estadoChat;
    private LocalDateTime fechaCompletada;
    private String comentarioCierre;
    private boolean calificacionHabilitada;
    private Integer calificacion;
    private Integer version;
    private boolean recordatorioRecogidaEnviado;

    public boolean requiereRecordatorioRecogida(LocalDateTime ahora) {
        return this.estado == EstadoReserva.CONFIRMADA
                && !this.recordatorioRecogidaEnviado
                && this.horaEstimadaEntrega != null
                && !ahora.isBefore(this.horaEstimadaEntrega.minusMinutes(15))
                && ahora.isBefore(this.horaEstimadaEntrega);
    }

    public void marcarRecordatorioRecogidaEnviado() {
        this.recordatorioRecogidaEnviado = true;
    }

    /**
     * HU-11: construye una reserva en estado PENDIENTE con hora límite.
     *
     * OC-138 — ReservaService.crear()
     * RN-33  — Calcula montoTotal = precio × cantidad
     * RN-04  — Fecha límite = ahora + 10 min
     */
    public static Reserva crear(Plato plato, Long compradorId, int cantidadPorciones,
                                MedioPago medioPago, String notaComprador, LocalDateTime ahora) {
        if (plato == null) throw new IllegalArgumentException("La reserva debe estar asociada a un plato");
        if (cantidadPorciones <= 0) throw new IllegalArgumentException("La cantidad de porciones debe ser mayor a 0");

        BigDecimal monto = plato.getPrecioPorcion() == null
                ? BigDecimal.ZERO
                : plato.getPrecioPorcion().multiply(BigDecimal.valueOf(cantidadPorciones));

        return Reserva.builder()
                .id(UUID.randomUUID())
                .platoId(plato.getId())
                .cocineraId(plato.getCocineraId())
                .compradorId(compradorId)
                .cantidadPorciones(cantidadPorciones)
                .montoTotal(monto)
                .medioPago(medioPago)
                .notaComprador(notaComprador)
                .estado(EstadoReserva.PENDIENTE)
                .estadoChat(EstadoChat.INACTIVO)
                .fechaCreacion(ahora)
                .fechaLimiteConfirmacion(ahora.plusMinutes(MINUTOS_PARA_CONFIRMAR))
                .build();
    }

    /**
     * HU-12: confirmación de la cocinera. Habilita el chat.
     *
     * OC-146 — ReservaService.confirmar()
     * RN-04  — Valida que no haya expirado
     * RN-17  — Activa el chat
     *
     * @throws ReservaNoPendienteException  si ya fue gestionada
     * @throws ReservaVencidaException      si pasaron los 10 min
     * @throws DecisionReservaInvalidaException si horaEstimada es nula o pasada
     */
    public void confirmar(LocalDateTime horaEstimada, LocalDateTime ahora) {
        validarQueSePuedeDecidir(ahora);
        if (horaEstimada == null || !horaEstimada.isAfter(ahora)) {
            throw new DecisionReservaInvalidaException(
                    "La hora estimada de entrega es obligatoria y debe ser posterior al momento actual");
        }
        this.estado = EstadoReserva.CONFIRMADA;
        this.horaEstimadaEntrega = horaEstimada;
        this.fechaDecision = ahora;
        this.chatHabilitado = true;
        this.estadoChat = EstadoChat.ACTIVO;
    }

    /**
     * HU-12: rechazo de la cocinera con motivo.
     *
     * OC-147 — ReservaService.rechazar() con liberación de porciones
     * RN-04  — Valida vigencia
     *
     * @throws DecisionReservaInvalidaException si motivo es nulo o si OTRO sin comentario
     */
    public void rechazar(MotivoRechazo motivo, String comentario, LocalDateTime ahora) {
        validarQueSePuedeDecidir(ahora);
        if (motivo == null) throw new DecisionReservaInvalidaException("El motivo de rechazo es obligatorio");

        String limpio = (comentario == null || comentario.isBlank()) ? null : comentario.trim();
        if (motivo == MotivoRechazo.OTRO && limpio == null)
            throw new DecisionReservaInvalidaException("El comentario es obligatorio cuando el motivo es OTRO");
        if (limpio != null && limpio.length() > MAX_CARACTERES_COMENTARIO)
            throw new DecisionReservaInvalidaException("El comentario no puede superar los " + MAX_CARACTERES_COMENTARIO + " caracteres");

        this.estado = EstadoReserva.RECHAZADA;
        this.motivoRechazo = motivo;
        this.comentarioRechazo = limpio;
        this.fechaDecision = ahora;
    }

    public void expirar(LocalDateTime ahora) {
        if (this.estado != EstadoReserva.PENDIENTE) throw new ReservaNoPendienteException(this.estado);
        if (!estaVencida(ahora)) throw new IllegalStateException("La reserva todavía está dentro del tiempo de confirmación");
        this.estado = EstadoReserva.EXPIRADA;
    }

    /**
     * HU-23: cierre de la transacción. Habilita la calificación.
     *
     * OC-156 — ReservaService.completar()
     * OC-159 — Cambio de estado del chat a SOLO_LECTURA (RN-17)
     * RN-32  — Solo desde CONFIRMADA
     *
     * @throws ReservaNoConfirmadaException si el estado no es CONFIRMADA
     */
    public void completar(String comentario, LocalDateTime ahora) {
        verificarQueEstaConfirmada();
        String limpio = (comentario == null || comentario.isBlank()) ? null : comentario.trim();
        if (limpio != null && limpio.length() > MAX_CARACTERES_COMENTARIO)
            throw new IllegalArgumentException("El comentario no puede superar los " + MAX_CARACTERES_COMENTARIO + " caracteres");

        this.estado = EstadoReserva.COMPLETADA;
        this.estadoChat = EstadoChat.SOLO_LECTURA;
        this.calificacionHabilitada = true;
        this.comentarioCierre = limpio;
        this.fechaCompletada = ahora;
    }

    public void verificarQueEstaConfirmada() {
        if (this.estado != EstadoReserva.CONFIRMADA) throw new ReservaNoConfirmadaException(this.estado);
    }

    /**
     * RN-33: si pasaron 24h desde la confirmación, corresponde cierre automático.
     *
     * OC-157 — Expiración automática a las 24h
     */
    public boolean cierreAutomaticoVencido(LocalDateTime ahora) {
        return this.estado == EstadoReserva.CONFIRMADA && this.fechaDecision != null
                && !ahora.isBefore(this.fechaDecision.plusHours(HORAS_PARA_CIERRE_AUTOMATICO));
    }

    /**
     * RN-25: si pasaron 7 min desde la creación y sigue pendiente, enviar recordatorio.
     *
     * <p>OC-149 — Recordatorio a los 7 min
     */
    public boolean requiereRecordatorio(LocalDateTime ahora) {
        return estaPendiente() && !this.recordatorioEnviado
                && !ahora.isBefore(this.fechaCreacion.plusMinutes(MINUTOS_PARA_RECORDATORIO))
                && !estaVencida(ahora);
    }

    public void marcarRecordatorioEnviado() { this.recordatorioEnviado = true; }
    public boolean estaPendiente() { return this.estado == EstadoReserva.PENDIENTE; }
    public boolean estaVencida(LocalDateTime ahora) { return !ahora.isBefore(this.fechaLimiteConfirmacion); }
    public boolean perteneceACocinera(UUID idCocinera) {
        return this.cocineraId != null && this.cocineraId.equals(idCocinera);
    }

    private void validarQueSePuedeDecidir(LocalDateTime ahora) {
        if (!estaPendiente()) throw new ReservaNoPendienteException(this.estado);
        if (estaVencida(ahora)) throw new ReservaVencidaException();
    }
    
}