package com.ollacercana.persistence.entities;

import com.ollacercana.core.models.enums.EstadoCuenta;
import com.ollacercana.core.models.enums.Rol;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "cuentas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private IdentidadEmbeddable identidad;

    @Embedded
    private CredencialesEmbeddable credenciales;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCuenta estado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cuenta_roles", joinColumns = @JoinColumn(name = "cuenta_id"))
    @Column(name = "rol")
    @Enumerated(EnumType.STRING)
    private Set<Rol> roles;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "avisos_activos", nullable = false)
    @Builder.Default
    private Boolean avisosActivos = true;

    @PrePersist
    protected void onCreate() {
        if (this.fechaRegistro == null) this.fechaRegistro = LocalDateTime.now();
        if (this.estado == null) this.estado = EstadoCuenta.ACTIVO;
        if (this.avisosActivos == null) this.avisosActivos = true;
        if (this.credenciales != null && this.credenciales.getCelularVerificado() == null) {
            this.credenciales.setCelularVerificado(false);
        }
    }
}