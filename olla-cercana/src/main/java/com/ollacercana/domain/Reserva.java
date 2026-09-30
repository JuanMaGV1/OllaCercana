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
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    /** RN-04: la cocinera tiene 10 minutos para confirmar o rechazar. */
    public static final int MINUTOS_PARA_CONFIRMAR = 10;

    /** RN-25: a los 7 minutos sin respuesta se le recuerda a la cocinera. */
    public static final int MINUTOS_PARA_RECORDATORIO = 7;

    public static final int MAX_CARACTERES_COMENTARIO = 150;

    /** HU-23 Escenario 2: si una parte no confirma, la reserva se completa sola a las 24 horas. */
    public static final int HORAS_PARA_CIERRE_AUTOMATICO = 24;

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID platoId;

    /** Id del perfil de la cocinera dueña del plato (igual que Plato.cocineraId). */
    @Column(nullable = false)
    private UUID cocineraId;

    /** Id de la cuenta del comprador. */
    @Column(nullable = false)
    private Long compradorId;

    @Column(nullable = false)
    private Integer cantidadPorciones;

    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    private MedioPago medioPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;

    @Column(length = 300)
    private String notaComprador;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaLimiteConfirmacion;

    /** Momento en que la cocinera confirmó o rechazó. */
    private LocalDateTime fechaDecision;

    /** HU-12: hora estimada de entrega que da la cocinera al confirmar. */
    private LocalDateTime horaEstimadaEntrega;

    @Enumerated(EnumType.STRING)
    private MotivoRechazo motivoRechazo;

    @Column(length = MAX_CARACTERES_COMENTARIO)
    private String comentarioRechazo;

    /** RN-25: evita enviar el recordatorio más de una vez. */
    private boolean recordatorioEnviado;

    /** HU-12: al confirmar se habilita la coordinación de la entrega (chat). */
    private boolean chatHabilitado;

    /** HU-23 / RN-17: ACTIVO al confirmar; SOLO_LECTURA cuando la reserva se completa. */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private com.ollacercana.domain.EstadoChat estadoChat = com.ollacercana.domain.EstadoChat.INACTIVO;

    /** HU-23: momento en que se cerró la transacción. */
    private LocalDateTime fechaCompletada;

    /** HU-23: comentario opcional al cerrar la transacción. */
    @Column(length = MAX_CARACTERES_COMENTARIO)
    private String comentarioCierre;

    /** HU-23: al completarse la reserva comprador y cocinera pueden calificarse. */
    private boolean calificacionHabilitada;

    @Version
    private Integer version;

    // ============ Reglas de negocio (RN) ============

    /**
     * Crea una reserva PENDIENTE con su hora límite de confirmación (RN-04).
     * No descuenta porciones: eso lo hace quien crea la reserva con Plato.comprometerPorciones().
     */
    public static Reserva crear(Plato plato, Long compradorId, int cantidadPorciones,
                                MedioPago medioPago, String notaComprador, LocalDateTime ahora) {
        if (plato == null) {
            throw new IllegalArgumentException("La reserva debe estar asociada a un plato");
        }
        if (cantidadPorciones <= 0) {
            throw new IllegalArgumentException("La cantidad de porciones debe ser mayor a 0");
        }

        BigDecimal monto = plato.getPrecioPorcion() == null
                ? null
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

    /**
     * HU-12 Escenario 1 / OC-146: la cocinera confirma.
     * Las porciones siguen descontadas del plato y se habilita la coordinación de la entrega.
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
        this.estadoChat = com.ollacercana.domain.EstadoChat.ACTIVO;
    }

    /**
     * HU-12 Escenario 2 / OC-147: la cocinera rechaza.
     * Quien llama debe devolver las porciones al plato (RN-03).
     */
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

    /**
     * RN-04 / OC-148: la cocinera no respondió a tiempo.
     * Quien llama debe devolver las porciones al plato.
     */
    public void expirar(LocalDateTime ahora) {
        if (this.estado != EstadoReserva.PENDIENTE) {
            throw new ReservaNoPendienteException(this.estado);
        }
        if (!estaVencida(ahora)) {
            throw new IllegalStateException("La reserva todavía está dentro del tiempo de confirmación");
        }
        this.estado = EstadoReserva.EXPIRADA;
    }

    /**
     * HU-23 Escenario 1 / OC-156: cierra la transacción de una reserva CONFIRMADA.
     * Pasa a COMPLETADA, el chat queda en solo lectura (RN-17) y se habilita la calificación.
     * El bloqueo por reporte abierto lo valida quien llama (necesita consultar los reportes).
     */
    public void completar(String comentario, LocalDateTime ahora) {
        verificarQueEstaConfirmada();

        String comentarioLimpio = (comentario == null || comentario.isBlank()) ? null : comentario.trim();
        if (comentarioLimpio != null && comentarioLimpio.length() > MAX_CARACTERES_COMENTARIO) {
            throw new IllegalArgumentException(
                    "El comentario no puede superar los " + MAX_CARACTERES_COMENTARIO + " caracteres");
        }

        this.estado = EstadoReserva.COMPLETADA;
        this.estadoChat = com.ollacercana.domain.EstadoChat.SOLO_LECTURA;
        this.calificacionHabilitada = true;
        this.comentarioCierre = comentarioLimpio;
        this.fechaCompletada = ahora;
    }

    /** HU-23 Escenario 4: solo una reserva CONFIRMADA se puede cerrar. */
    public void verificarQueEstaConfirmada() {
        if (this.estado != EstadoReserva.CONFIRMADA) {
            throw new ReservaNoConfirmadaException(this.estado);
        }
    }

    /**
     * HU-23 Escenario 2 / OC-157: confirmada hace 24 horas o más y sin cierre.
     */
    public boolean cierreAutomaticoVencido(LocalDateTime ahora) {
        return this.estado == EstadoReserva.CONFIRMADA
                && this.fechaDecision != null
                && !ahora.isBefore(this.fechaDecision.plusHours(HORAS_PARA_CIERRE_AUTOMATICO));
    }

    /**
     * RN-25 / OC-149: pendiente, sin recordatorio previo, con 7 minutos o más y aún no vencida.
     */
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

    /** RN-04: vencida cuando ya se llegó a la hora límite de confirmación. */
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
