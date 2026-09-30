package com.ollacercana.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reporte sobre una reserva (OC-34). Mientras esté ABIERTO no se puede cerrar la transacción (HU-23).
 */
@Entity
@Table(name = "reportes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID reservaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.ollacercana.domain.EstadoReporte estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;
}
