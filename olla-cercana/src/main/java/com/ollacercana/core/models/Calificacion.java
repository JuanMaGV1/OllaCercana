package com.ollacercana.core.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * OC-193: dominio de una calificación publicada por un comprador tras
 * completar una reserva (HU-31).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Calificacion {

    private UUID id;
    private UUID reservaId;
    private Long compradorId;
    private UUID cocineraId;
    private Integer estrellas;
    private String comentario;
    private LocalDateTime fechaCreacion;

    /** RN-31.4: la calificación es "positiva" cuando tiene 4 o 5 estrellas. */
    public boolean esPositiva() {
        return estrellas != null && estrellas >= 4;
    }
}