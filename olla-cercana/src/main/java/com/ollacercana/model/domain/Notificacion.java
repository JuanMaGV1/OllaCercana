package com.ollacercana.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID reservaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rolDestinatario;

    /** Se llena cuando el destinatario es el comprador. */
    private Long compradorId;

    /** Se llena cuando el destinatario es la cocinera. */
    private UUID cocineraId;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacion tipo;

    private boolean leida;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    public void marcarLeida() {
        this.leida = true;
    }

    @PrePersist
    @PreUpdate
    protected void validarDestinatario() {
        if (compradorId == null && cocineraId == null) {
            throw new IllegalStateException("La notificación debe tener un destinatario");
        }
        if (compradorId != null && cocineraId != null) {
            throw new IllegalStateException("La notificación no puede tener ambos destinatarios");
        }
        if (fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}
