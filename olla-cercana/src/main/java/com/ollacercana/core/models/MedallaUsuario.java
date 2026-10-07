package com.ollacercana.core.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * HU-21 / OC-278: medalla otorgada a un usuario. {@code vigenteHasta} es opcional:
 * sin fecha la medalla no vence.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedallaUsuario {

    private UUID id;
    private Long usuarioId;
    private Medalla medalla;
    private LocalDateTime fechaOtorgada;
    private LocalDateTime vigenteHasta;

    public boolean estaVigente(LocalDateTime ahora) {
        return vigenteHasta == null || vigenteHasta.isAfter(ahora);
    }
}