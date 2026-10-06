package com.ollacercana.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "perfiles_cocinera")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilCocinera {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 500)
    private String presentacion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "perfil_especialidades", joinColumns = @JoinColumn(name = "perfil_id"))
    @Column(name = "especialidad")
    private List<String> especialidades;

    @ElementCollection(fetch = FetchType.EAGER)
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
    @Column(name = "verificada")
    private boolean verificada = false;

    @Builder.Default
    @Column(name = "pausada")
    private boolean pausada = false;

    @Column(name = "fecha_reactivacion")
    private LocalDateTime fechaReactivacion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", unique = true)
    private Cuenta cuenta;

    // Métodos puente para mantener compatibilidad con CocineraQueryPort
    public boolean verificada() {
        return this.verificada;
    }

    public boolean pausada() {
        return this.pausada;
    }
}