package com.ollacercana.persistence.entity;

import com.ollacercana.model.domain.EstadoCuenta;
import com.ollacercana.model.domain.Rol;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "cuentas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaEntity implements Persistable<UUID> {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false, unique = true, length = 10)
    private String celular;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "contrasena_hash", nullable = false)
    private String contrasenaHash;

    @Column(name = "token_fcm")
    private String tokenFCM;

    @Column(name = "celular_verificado", nullable = false)
    private Boolean celularVerificado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCuenta estado;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "cuenta_roles", joinColumns = @JoinColumn(name = "cuenta_id"))
    @Column(name = "rol")
    @Enumerated(EnumType.STRING)
    private Set<Rol> roles;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

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

    // ============ Defaults ============

    @PrePersist
    protected void onCreate() {
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.fechaRegistro == null) this.fechaRegistro = LocalDateTime.now();
        if (this.estado == null) this.estado = EstadoCuenta.PENDIENTE_VERIFICACION;
        if (this.celularVerificado == null) this.celularVerificado = false;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}