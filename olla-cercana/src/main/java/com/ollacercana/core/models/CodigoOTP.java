package com.ollacercana.core.models;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodigoOTP {

    private Long id;
    private UUID perfilId;
    private String codigo;
    private LocalDateTime fechaExpiracion;
    private boolean usado;

    public boolean esValido(String codigoIngresado) {
        return !usado && this.codigo.equals(codigoIngresado) && LocalDateTime.now().isBefore(fechaExpiracion);
    }
}