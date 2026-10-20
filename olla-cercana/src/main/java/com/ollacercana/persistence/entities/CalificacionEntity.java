package com.ollacercana.persistence.entities;

import com.ollacercana.core.models.enums.EstadoCalificacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "calificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CalificacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reserva_id", nullable = false, unique = true)
    private UUID reservaId;

    @Column(name = "comprador_id", nullable = false)
    private Long compradorId;

    @Column(name = "cocinera_id", nullable = false)
    private UUID cocineraId;

    @Column(nullable = false)
    private Integer estrellas;

    @Column(length = 500)
    private String comentario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCalificacion estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_limite_publicacion", nullable = false)
    private LocalDateTime fechaLimitePublicacion;
}