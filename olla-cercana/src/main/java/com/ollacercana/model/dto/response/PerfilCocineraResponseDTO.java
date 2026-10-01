package com.ollacercana.model.dto.response;

import com.ollacercana.model.domain.MedioPago;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilCocineraResponseDTO {

    private UUID id;
    private UUID cuentaId;
    private String nombreCocinera;
    private String presentacion;
    private String conjuntoResidencial;
    private List<String> especialidades;
    private List<MedioPago> mediosPago;
    private String numeroNequi;
    private String numeroDaviplata;
    private Double promedioCalificacion;
    private Integer resenasPositivas;
    private Boolean esDestacada;
    private boolean verificada;
    private boolean pausada;
}