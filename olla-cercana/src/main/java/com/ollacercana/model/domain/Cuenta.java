package com.ollacercana.model.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cuenta {

    private Long id;
    private Identidad identidad;
    private Credenciales credenciales;
    private EstadoCuenta estado;
    private Set<Rol> roles;
    private LocalDateTime fechaRegistro;

    /** Inicializa valores por defecto al crear una cuenta nueva. */
    public void inicializar() {
        if (this.fechaRegistro == null) this.fechaRegistro = LocalDateTime.now();
        if (this.estado == null) this.estado = EstadoCuenta.ACTIVO;
        if (this.credenciales != null && this.credenciales.getCelularVerificado() == null) {
            this.credenciales.setCelularVerificado(false);
        }
    }
}