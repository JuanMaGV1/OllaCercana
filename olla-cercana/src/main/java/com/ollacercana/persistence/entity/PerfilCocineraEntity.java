package com.ollacercana.persistence.entity;

import com.ollacercana.model.domain.MedioPago;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "perfiles_cocinera")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilCocineraEntity implements Persistable<UUID> {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "cuenta_id", nullable = false, unique = true)
    private UUID cuentaId;

    @Column(length = 500)
    private String presentacion;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "perfil_especialidades", joinColumns = @JoinColumn(name = "perfil_id"))
    @Column(name = "especialidad")
    private List<String> especialidades;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "perfil_medios_pago", joinColumns = @JoinColumn(name = "perfil_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago")
    private List<MedioPago> mediosPago;

    @Column(name = "numero_nequi", length = 15)
    private String numeroNequi;

    @Column(name = "numero_daviplata", length = 15)
    private String numeroDaviplata;

    @Column(name = "conjunto_residencial", nullable = false)
    private String conjuntoResidencial;

    @Builder.Default
    @Column(name = "promedio_calificacion")
    private Double promedioCalificacion = 0.0;

    @Builder.Default
    @Column(name = "resenas_positivas")
    private Integer resenasPositivas = 0;

    @Builder.Default
    @Column(name = "es_destacada")
    private Boolean esDestacada = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean verificada = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean pausada = false;

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
        if (this.promedioCalificacion == null) this.promedioCalificacion = 0.0;
        if (this.resenasPositivas == null) this.resenasPositivas = 0;
        if (this.esDestacada == null) this.esDestacada = false;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}