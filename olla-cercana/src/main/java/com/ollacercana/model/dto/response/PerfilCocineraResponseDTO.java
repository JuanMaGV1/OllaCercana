package com.ollacercana.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

import com.ollacercana.model.domain.MedioPago;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos públicos del perfil de la cocinera")
public class PerfilCocineraResponseDTO {

    private UUID id;
    private Long cuentaId;
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