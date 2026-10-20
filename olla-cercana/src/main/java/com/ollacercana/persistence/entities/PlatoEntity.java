package com.ollacercana.persistence.entities;

import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.RestriccionAlimentaria;
import com.ollacercana.core.models.enums.TipoComida;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA de Plato — sólo estructura de tabla (sin lógica de negocio).
 * Dominio ↔ Entidad se traducen con PlatoEntityMapper.
 */
@Entity
@Table(name = "platos")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PlatoEntity {

    @Id
    private UUID id;

    @Column(name = "cocinera_id", nullable = false)
    private UUID cocineraId;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comida", nullable = false)
    private TipoComida tipoComida;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "plato_restricciones", joinColumns = @JoinColumn(name = "plato_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "restriccion")
    private List<RestriccionAlimentaria> restricciones;

    @Column(name = "porciones_totales", nullable = false)
    private Integer porcionesTotales;

    @Column(name = "porciones_comprometidas")
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

    @Version
    private Integer version;
}