package com.ollacercana.model.domain;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    private Long cuentaId;   // ← FK por ID, no referencia a Cuenta completa
    private String nombreCocinera; // proyección de Cuenta.identidad.nombre (opcional)

    public boolean verificada() { return verificada; }
    public boolean pausada() { return pausada; }
}