package com.ollacercana.domain;

import jakarta.persistence.*;
import lombok.*;

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
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObjetivoReporte objetivo;

    @Column(nullable = true)
    private UUID objetivoId;                                                                       

    @Column(name = "cuenta_objetivo_id")
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

    @Column(nullable = false)
    private Long reportanteId;

    @Column(nullable = true)
    private UUID reservaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.ollacercana.domain.EstadoReporte estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

}
