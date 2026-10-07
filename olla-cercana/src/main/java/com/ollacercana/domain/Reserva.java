package com.ollacercana.domain;

import com.ollacercana.exception.DecisionReservaInvalidaException;
import com.ollacercana.exception.ReservaNoConfirmadaException;
import com.ollacercana.exception.ReservaNoPendienteException;
import com.ollacercana.exception.ReservaVencidaException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Reserva {

    public static final int MINUTOS_PARA_CONFIRMAR = 10;
    public static final int MINUTOS_PARA_RECORDATORIO = 7;
    public static final int MAX_CARACTERES_COMENTARIO = 150;
    public static final int HORAS_PARA_CIERRE_AUTOMATICO = 24;

    // ID asignado por la aplicación (Reserva.crear lo genera), igual que Plato.
    // Con @GeneratedValue + @Version, Hibernate rechaza persistir un id ya asignado.
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID platoId;

    @Column(nullable = false)
    private UUID cocineraId;

    @Column(nullable = false)
    private Long compradorId;

    @Column(nullable = false)
    private Integer cantidadPorciones;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    private MedioPago medioPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;

    @Column(length = 500)
    private String notaComprador;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaLimiteConfirmacion;

    private LocalDateTime fechaDecision;

    private LocalDateTime horaEstimadaEntrega;

    @Enumerated(EnumType.STRING)
    private MotivoRechazo motivoRechazo;

    @Column(length = MAX_CARACTERES_COMENTARIO)
    private String comentarioRechazo;

    private boolean recordatorioEnviado;

    private boolean chatHabilitado;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoChat estadoChat = EstadoChat.INACTIVO;

    private LocalDateTime fechaCompletada;

    @Column(length = MAX_CARACTERES_COMENTARIO)
    private String comentarioCierre;

    private boolean calificacionHabilitada;

    // Calificación (1-5) que el comprador le dio a la cocinera; nula mientras no la haya publicado.
    private Integer calificacion;

    @Version
    private Integer version;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public static Reserva crear(Plato plato, Long compradorId, int cantidadPorciones,
                                MedioPago medioPago, String notaComprador, LocalDateTime ahora) {
        if (plato == null) {
            throw new IllegalArgumentException("La reserva debe estar asociada a un plato");
        }
        if (cantidadPorciones <= 0) {
            throw new IllegalArgumentException("La cantidad de porciones debe ser mayor a 0");
        }

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
        if (motivo == null) {
            throw new DecisionReservaInvalidaException("El motivo de rechazo es obligatorio");
        }

        String comentarioLimpio = (comentario == null || comentario.isBlank()) ? null : comentario.trim();
        if (motivo == MotivoRechazo.OTRO && comentarioLimpio == null) {
            throw new DecisionReservaInvalidaException("El comentario es obligatorio cuando el motivo es OTRO");
        }
        if (comentarioLimpio != null && comentarioLimpio.length() > MAX_CARACTERES_COMENTARIO) {
            throw new DecisionReservaInvalidaException(
                    "El comentario no puede superar los " + MAX_CARACTERES_COMENTARIO + " caracteres");
        }

        this.estado = EstadoReserva.RECHAZADA;
        this.motivoRechazo = motivo;
        this.comentarioRechazo = comentarioLimpio;
        this.fechaDecision = ahora;
    }

    public void expirar(LocalDateTime ahora) {
        if (this.estado != EstadoReserva.PENDIENTE) {
            throw new ReservaNoPendienteException(this.estado);
        }
        if (!estaVencida(ahora)) {
            throw new IllegalStateException("La reserva todavía está dentro del tiempo de confirmación");
        }
        this.estado = EstadoReserva.EXPIRADA;
    }

    public void completar(String comentario, LocalDateTime ahora) {
        verificarQueEstaConfirmada();

        String comentarioLimpio = (comentario == null || comentario.isBlank()) ? null : comentario.trim();
        if (comentarioLimpio != null && comentarioLimpio.length() > MAX_CARACTERES_COMENTARIO) {
            throw new IllegalArgumentException(
                    "El comentario no puede superar los " + MAX_CARACTERES_COMENTARIO + " caracteres");
        }

        this.estado = EstadoReserva.COMPLETADA;
        this.estadoChat = EstadoChat.SOLO_LECTURA;
        this.calificacionHabilitada = true;
        this.comentarioCierre = comentarioLimpio;
        this.fechaCompletada = ahora;
    }

    public void verificarQueEstaConfirmada() {
        if (this.estado != EstadoReserva.CONFIRMADA) {
            throw new ReservaNoConfirmadaException(this.estado);
        }
    }

    public boolean cierreAutomaticoVencido(LocalDateTime ahora) {
        return this.estado == EstadoReserva.CONFIRMADA
                && this.fechaDecision != null
                && !ahora.isBefore(this.fechaDecision.plusHours(HORAS_PARA_CIERRE_AUTOMATICO));
    }

    public boolean requiereRecordatorio(LocalDateTime ahora) {
        return estaPendiente()
                && !this.recordatorioEnviado
                && !ahora.isBefore(this.fechaCreacion.plusMinutes(MINUTOS_PARA_RECORDATORIO))
                && !estaVencida(ahora);
    }

    public void marcarRecordatorioEnviado() {
        this.recordatorioEnviado = true;
    }

    public boolean estaPendiente() {
        return this.estado == EstadoReserva.PENDIENTE;
    }

    public boolean estaVencida(LocalDateTime ahora) {
        return !ahora.isBefore(this.fechaLimiteConfirmacion);
    }

    public boolean perteneceACocinera(UUID idCocinera) {
        return this.cocineraId != null && this.cocineraId.equals(idCocinera);
    }

    private void validarQueSePuedeDecidir(LocalDateTime ahora) {
        if (!estaPendiente()) {
            throw new ReservaNoPendienteException(this.estado);
        }
        if (estaVencida(ahora)) {
            throw new ReservaVencidaException();
        }
    }
}