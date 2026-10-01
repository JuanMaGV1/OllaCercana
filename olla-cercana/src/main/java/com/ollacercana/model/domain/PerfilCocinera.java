package com.ollacercana.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilCocinera {

    private UUID id;
    private UUID cuentaId;         // referencia por id, NO objeto Cuenta
    private String presentacion;
    private List<String> especialidades;
    private List<MedioPago> mediosPago;
    private String numeroNequi;
    private String numeroDaviplata;
    private String conjuntoResidencial;
    private Double promedioCalificacion;
    private Integer resenasPositivas;
    private Boolean esDestacada;
    private boolean verificada;
    private boolean pausada;

    public boolean verificada() { return verificada; }
    public boolean pausada() { return pausada; }

    public void marcarVerificada() {
        this.verificada = true;
    }

    public void pausar() {
        this.pausada = true;
    }

    public void reactivar() {
        this.pausada = false;
    }

    public void evaluarDestacada(int resenasMinimas) {
        if (resenasPositivas != null && resenasPositivas >= resenasMinimas) {
            this.esDestacada = true;
        }
    }
}