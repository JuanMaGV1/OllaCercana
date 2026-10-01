package com.ollacercana.persistence.entity;

import com.ollacercana.model.domain.EstadoReporte;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reportes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteEntity implements Persistable<UUID> {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "reserva_id", nullable = false)
    private UUID reservaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReporte estado;

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
        if (this.estado == null) this.estado = EstadoReporte.ABIERTO;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}