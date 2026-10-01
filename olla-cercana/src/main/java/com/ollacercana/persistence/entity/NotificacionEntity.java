package com.ollacercana.persistence.entity;

import com.ollacercana.model.domain.Rol;
import com.ollacercana.model.domain.TipoNotificacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reserva_id")
    private UUID reservaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_destinatario", nullable = false)
    private Rol rolDestinatario;

    @Column(name = "comprador_id")
    private UUID compradorId;

    @Column(name = "cocinera_id")
    private UUID cocineraId;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacion tipo;

    @Column(nullable = false)
    private boolean leida;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}