package com.ollacercana.persistence.entities;

import com.ollacercana.core.models.enums.EstadoReporte;
import com.ollacercana.core.models.enums.MotivoReporte;
import com.ollacercana.core.models.enums.ObjetivoReporte;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reportes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObjetivoReporte objetivo;

    @Column(name = "objetivo_id", nullable = true)
    private UUID objetivoId;

    @Column(name = "cuenta_objetivo_id", nullable = true)
    private Long cuentaObjetivoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivoReporte motivo;

    @Column(length = 1000)
    private String descripcion;

    @ElementCollection
    @CollectionTable(name = "reporte_evidencias", joinColumns = @JoinColumn(name = "reporte_id"))
    @Column(name = "evidencia_url")
    private List<String> evidencias;

    @Column(name = "reportante_id", nullable = false)
    private Long reportanteId;

    @Column(name = "reserva_id", nullable = true)
    private UUID reservaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReporte estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}