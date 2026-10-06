package com.ollacercana.persistence.entity;

import com.ollacercana.model.domain.*;
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
public class ReservaEntity {

    @Id
    private UUID id;

    @Column(name = "plato_id", nullable = false)       private UUID platoId;
    @Column(name = "cocinera_id", nullable = false)    private UUID cocineraId;
    @Column(name = "comprador_id", nullable = false)   private Long compradorId;
    @Column(name = "cantidad_porciones", nullable = false) private Integer cantidadPorciones;

    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING) private MedioPago medioPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;

    @Column(name = "nota_comprador", length = 500) private String notaComprador;

    @Column(name = "fecha_creacion", nullable = false) private LocalDateTime fechaCreacion;
    @Column(name = "fecha_limite_confirmacion", nullable = false) private LocalDateTime fechaLimiteConfirmacion;
    @Column(name = "fecha_decision") private LocalDateTime fechaDecision;
    @Column(name = "hora_estimada_entrega") private LocalDateTime horaEstimadaEntrega;

    @Enumerated(EnumType.STRING) private MotivoRechazo motivoRechazo;

    @Column(name = "comentario_rechazo", length = 150) private String comentarioRechazo;

    @Column(name = "recordatorio_enviado") private boolean recordatorioEnviado;
    @Column(name = "chat_habilitado") private boolean chatHabilitado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_chat")
    private EstadoChat estadoChat;

    @Column(name = "fecha_completada") private LocalDateTime fechaCompletada;
    @Column(name = "comentario_cierre", length = 150) private String comentarioCierre;
    @Column(name = "calificacion_habilitada") private boolean calificacionHabilitada;

    @Version private Integer version;
}