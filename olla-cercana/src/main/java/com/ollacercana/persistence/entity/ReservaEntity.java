package com.ollacercana.persistence.entity;

import com.ollacercana.model.domain.*;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaEntity implements Persistable<UUID> {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "plato_id", nullable = false)
    private UUID platoId;

    @Column(name = "cocinera_id", nullable = false)
    private UUID cocineraId;

    @Column(name = "comprador_id", nullable = false)
    private UUID compradorId;

    @Column(name = "cantidad_porciones", nullable = false)
    private Integer cantidadPorciones;

    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago")
    private MedioPago medioPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;

    @Column(name = "nota_comprador", length = 300)
    private String notaComprador;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_limite_confirmacion", nullable = false)
    private LocalDateTime fechaLimiteConfirmacion;

    @Column(name = "fecha_decision")
    private LocalDateTime fechaDecision;

    @Column(name = "hora_estimada_entrega")
    private LocalDateTime horaEstimadaEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_rechazo")
    private MotivoRechazo motivoRechazo;

    @Column(name = "comentario_rechazo", length = 150)
    private String comentarioRechazo;

    @Column(name = "recordatorio_enviado", nullable = false)
    private boolean recordatorioEnviado;

    @Column(name = "chat_habilitado", nullable = false)
    private boolean chatHabilitado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_chat", nullable = false)
    private EstadoChat estadoChat;

    @Column(name = "fecha_completada")
    private LocalDateTime fechaCompletada;

    @Column(name = "comentario_cierre", length = 150)
    private String comentarioCierre;

    @Column(name = "calificacion_habilitada", nullable = false)
    private boolean calificacionHabilitada;

    // ============ Persistable ============

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.fechaCreacion == null) this.fechaCreacion = LocalDateTime.now();
        if (this.fechaLimiteConfirmacion == null) 
            this.fechaLimiteConfirmacion = this.fechaCreacion.plusMinutes(10);
        if (this.estado == null) this.estado = EstadoReserva.PENDIENTE;
        if (this.estadoChat == null) this.estadoChat = EstadoChat.ABIERTO;
        if (this.montoTotal == null) this.montoTotal = BigDecimal.ZERO;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}