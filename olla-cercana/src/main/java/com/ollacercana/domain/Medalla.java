package com.ollacercana.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HU-21 / OC-278: catálogo de medallas (código, nombre y requisito para obtenerla).
 */
@Entity
@Table(name = "medallas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medalla {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private CodigoMedalla codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 300)
    private String requisito;
}
