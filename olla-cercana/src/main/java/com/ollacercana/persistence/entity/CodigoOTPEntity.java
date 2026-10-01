package com.ollacercana.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "codigos_otp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoOTPEntity implements Persistable<UUID> {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "perfil_id", nullable = false)
    private UUID perfilId;

    @Column(nullable = false, length = 10)
    private String codigo;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private boolean usado;

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
        if (this.fechaExpiracion == null) this.fechaExpiracion = LocalDateTime.now().plusMinutes(10);
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}