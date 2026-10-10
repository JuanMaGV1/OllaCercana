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
 * Dominio puro de Reserva — SIN anotaciones JPA.
 * Las columnas viven en persistence.entity.ReservaEntity.
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

    public boolean cierreAutomaticoVencido(LocalDateTime ahora) {
        return this.estado == EstadoReserva.CONFIRMADA && this.fechaDecision != null
                && !ahora.isBefore(this.fechaDecision.plusHours(HORAS_PARA_CIERRE_AUTOMATICO));
    }

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