package com.ollacercana.persistence.entity;

import com.ollacercana.model.domain.EstadoPlato;
import com.ollacercana.model.domain.RestriccionAlimentaria;
import com.ollacercana.model.domain.TipoComida;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "platos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatoEntity implements Persistable<UUID> {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "cocinera_id", nullable = false)
    private UUID cocineraId;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 300)
    private String descripcion;

    @Column(name = "foto_url", nullable = false)
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comida", nullable = false, length = 20)
    private TipoComida tipoComida;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "plato_restricciones", joinColumns = @JoinColumn(name = "plato_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "restriccion")
    private List<RestriccionAlimentaria> restricciones;

    @Column(name = "porciones_totales", nullable = false)
    private Integer porcionesTotales;

    @Column(name = "porciones_comprometidas", nullable = false)
    private Integer porcionesComprometidas;

    @Column(name = "precio_porcion", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPorcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPlato estado;

    @Column(name = "hora_disponibilidad")
    private LocalDateTime horaDisponibilidad;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    private Double latitud;
    private Double longitud;

    @Column(name = "punto_entrega")
    private String puntoEntrega;

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
        if (this.estado == null) this.estado = EstadoPlato.ACTIVO;
        if (this.porcionesComprometidas == null) this.porcionesComprometidas = 0;
        if (this.fechaPublicacion == null) this.fechaPublicacion = LocalDateTime.now();
        if (this.fechaExpiracion == null) 
            this.fechaExpiracion = this.fechaPublicacion.plusHours(4);
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}