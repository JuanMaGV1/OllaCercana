package com.ollacercana.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "decisiones_moderacion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionModeracion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDecision decision;

    @Column(nullable = false, length = 1000)
    private String justificacion;

    @Column(nullable = false)
    private Long administradorId;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporte_id", nullable = true)
    private Reporte reporte;
}
