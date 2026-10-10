package com.ollacercana.core.models;

import com.ollacercana.core.models.enums.CodigoMedalla;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HU-21 / OC-278: catálogo de medallas (código, nombre y requisito para obtenerla).
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medalla {

    private CodigoMedalla codigo;
    private String nombre;
    private String requisito;
    
}