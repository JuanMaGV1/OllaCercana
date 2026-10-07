package com.ollacercana.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "medallas_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedallaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "medalla_codigo", nullable = false)
    private Medalla medalla;

    @Column(nullable = false)
    private LocalDateTime fechaOtorgada;

    private LocalDateTime vigenteHasta;

    public boolean estaVigente(LocalDateTime ahora) {
        return vigenteHasta == null || vigenteHasta.isAfter(ahora);
    }
}
