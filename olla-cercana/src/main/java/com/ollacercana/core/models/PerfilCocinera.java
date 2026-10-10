package com.ollacercana.core.models;

import com.ollacercana.core.models.enums.MedioPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilCocinera {

    private UUID id;
    private String presentacion;
    private List<String> especialidades;
    private List<MedioPago> mediosPago;
    private String numeroNequi;
    private String numeroDaviplata;
    private String conjuntoResidencial;
    @Builder.Default private Double promedioCalificacion = 0.0;
    @Builder.Default private Integer resenasPositivas = 0;
    @Builder.Default private Boolean esDestacada = false;
    @Builder.Default private boolean verificada = false;
    @Builder.Default private boolean pausada = false;
    private LocalDateTime fechaReactivacion;
    private Cuenta cuenta;

    public boolean verificada() { return this.verificada; }
    public boolean pausada() { return this.pausada; }
    
}