package com.ollacercana.persistence.entity;

import com.ollacercana.model.domain.Rol;
import com.ollacercana.model.domain.TipoNotificacion;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionEntity implements Persistable<UUID> {

    @Id
    @Column(nullable = false, updatable = false)
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
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}