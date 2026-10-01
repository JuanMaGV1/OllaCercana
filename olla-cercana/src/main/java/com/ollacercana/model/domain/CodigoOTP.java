package com.ollacercana.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoOTP {

    private UUID id;
    private UUID perfilId;
    private String codigo;
    private LocalDateTime fechaExpiracion;
    private boolean usado;

    public boolean esValido(String codigoIngresado) {
        return !usado
                && this.codigo != null
                && this.codigo.equals(codigoIngresado)
                && LocalDateTime.now().isBefore(fechaExpiracion);
    }

    public void marcarUsado() {
        this.usado = true;
    }
}