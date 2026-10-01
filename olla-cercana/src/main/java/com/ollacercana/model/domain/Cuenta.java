package com.ollacercana.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuenta {

    private UUID id;
    private Identidad identidad;
    private Credenciales credenciales;
    private EstadoCuenta estado;
    private Set<Rol> roles;
    private LocalDateTime fechaRegistro;

    // ============ Reglas de negocio ============

    public void activar() {
        this.estado = EstadoCuenta.ACTIVA;
    }

    public void pausar() {
        this.estado = EstadoCuenta.PAUSADA;
    }

    public void suspender() {
        this.estado = EstadoCuenta.SUSPENDIDA;
    }

    public void bloquearTemporalmente() {
        this.estado = EstadoCuenta.BLOQUEADA_TEMPORAL;
    }

    public boolean tieneRol(Rol rol) {
        return roles != null && roles.contains(rol);
    }

    public void agregarRol(Rol rol) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(rol);
    }

    public boolean estaActiva() {
        return this.estado == EstadoCuenta.ACTIVA;
    }

    public boolean estaPendienteVerificacion() {
        return this.estado == EstadoCuenta.PENDIENTE_VERIFICACION;
    }
}