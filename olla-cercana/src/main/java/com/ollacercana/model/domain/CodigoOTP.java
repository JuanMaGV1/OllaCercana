package com.ollacercana.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "codigos_otp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoOTP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID perfilId;

    @Column(nullable = false, length = 10)
    private String codigo;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private boolean usado;

    public boolean esValido(String codigoIngresado) {
        return !usado && this.codigo.equals(codigoIngresado) && LocalDateTime.now().isBefore(fechaExpiracion);
    }
}