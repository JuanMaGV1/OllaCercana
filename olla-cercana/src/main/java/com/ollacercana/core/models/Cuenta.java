package com.ollacercana.core.models;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

import com.ollacercana.core.models.enums.EstadoCuenta;
import com.ollacercana.core.models.enums.Rol;

/**
 * Cuenta de usuario en la plataforma. Es el agregado raíz de la autenticación.
 *
 * FEAT-04 — Autenticación / Perfil (OC-43)
 * HU-01   — Registro de cuenta (OC-17)
 * HU-02   — Inicio de sesión (OC-18)
 * OC-59   — Entidad Cuenta + Identidad + Credenciales
 * RN-01   — Verificación de celular obligatoria para publicar/reservar
 */

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
    private Boolean avisosActivos;

    /**
     * Inicializa los valores por defecto al crear una cuenta nueva.
     *
     * OC-59  — Evita violaciones NOT NULL en la tabla cuentas
     * RN-01  — Fuerza {@code celularVerificado=false} hasta pasar por OTP
     * RN-26  — Avisos activados por defecto (solo chat es configurable)
     */
    public void inicializar() {
        if (this.fechaRegistro == null) this.fechaRegistro = LocalDateTime.now();
        if (this.estado == null) this.estado = EstadoCuenta.ACTIVO;
        if (this.credenciales != null && this.credenciales.getCelularVerificado() == null) {
            this.credenciales.setCelularVerificado(false);
        }
        if (this.avisosActivos == null) this.avisosActivos = true;
    }
}