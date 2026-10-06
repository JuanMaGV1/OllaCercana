package com.ollacercana.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObjetivoReporte objetivo;

    @Column(nullable = false)
    private UUID objetivoId; // ID of the Plato or Cuenta being reported

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivoReporte motivo;

    @Column(length = 1000)
    private String descripcion;

    @ElementCollection
    @CollectionTable(name = "reporte_evidencias", joinColumns = @JoinColumn(name = "reporte_id"))
    @Column(name = "evidencia_url")
    private List<String> evidencias;

    @Column(nullable = false)
    private UUID reportanteId;

    @Column(nullable = true)
    private UUID reservaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.ollacercana.domain.EstadoReporte estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

}
