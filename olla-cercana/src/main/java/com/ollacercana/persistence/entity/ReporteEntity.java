package com.ollacercana.persistence.entity;

import com.ollacercana.model.domain.EstadoReporte;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reportes")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReporteEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false) private UUID reservaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private EstadoReporte estado;

    @Column(nullable = false) private LocalDateTime fechaCreacion;
}