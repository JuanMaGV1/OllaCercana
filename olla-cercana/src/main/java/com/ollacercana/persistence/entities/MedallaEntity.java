package com.ollacercana.persistence.entities;

import com.ollacercana.core.models.enums.CodigoMedalla;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "medallas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedallaEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private CodigoMedalla codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 300)
    private String requisito;
}