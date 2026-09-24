package com.ollacercana.domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plato {

    private UUID id;
    private String nombre;
    private String descripcion;
    private String fotoUrl;
    private TipoComida tipoComida;
    private List<RestriccionAlimentaria> restricciones;
    private Integer porcionesTotales;
    private Integer porcionesComprometidas;
    private BigDecimal precioPorcion;
    private EstadoPlato estado;
    private LocalDateTime fechaPublicacion;
    private LocalDateTime fechaExpiracion;
    private String puntoEntrega;
    private Integer version;

    // ============ Reglas de negocio (RN) ============

    /**
     * RN-03: porciones disponibles = totales - comprometidas.
     */
    public int getPorcionesDisponibles() {
        int comprometidas = porcionesComprometidas == null ? 0 : porcionesComprometidas;
        return porcionesTotales - comprometidas;
    }

    /**
     * RN-02: un plato expira a las 4 horas de publicación.
     * NOTA: el id lo asigna el repositorio en memoria, NO el dominio.
     */
    public void publicar() {
        this.estado = EstadoPlato.ACTIVO;
        this.porcionesComprometidas = 0;
        this.fechaPublicacion = LocalDateTime.now();
        this.fechaExpiracion = this.fechaPublicacion.plusHours(4);
        this.version = 0;
    }

    /**
     * RN-03: cuando las disponibles llegan a 0, el plato pasa a AGOTADO.
     */
    public void marcarAgotado() {
        this.estado = EstadoPlato.AGOTADO;
    }

    /**
     * RN-02: expiración automática.
     */
    public void expirar() {
        this.estado = EstadoPlato.EXPIRADO;
    }

    /**
     * RN-03: recalcula el estado según las porciones disponibles.
     */
    public void recalcularEstado() {
        if (this.estado == EstadoPlato.EXPIRADO) return;
        if (getPorcionesDisponibles() <= 0) {
            this.estado = EstadoPlato.AGOTADO;
        } else if (this.estado == EstadoPlato.AGOTADO) {
            this.estado = EstadoPlato.ACTIVO;
        }
    }

    /**
     * RN-03: descuenta porciones comprometidas (al reservar).
     */
    public void comprometerPorciones(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (cantidad > getPorcionesDisponibles()) {
            throw new IllegalStateException(
                "No hay suficientes porciones disponibles. Disponibles: " + getPorcionesDisponibles()
            );
        }
        this.porcionesComprometidas += cantidad;
        recalcularEstado();
    }

    /**
     * RN-03: libera porciones comprometidas (al cancelar/rechazar reserva).
     */
    public void liberarPorciones(int cantidad) {
        this.porcionesComprometidas = Math.max(0, this.porcionesComprometidas - cantidad);
        recalcularEstado();
    }

    /**
     * OC-005: cambia el total de porciones con validación.
     */
    public void cambiarPorcionesTotales(int nuevaCantidad) {
        if (nuevaCantidad < 1 || nuevaCantidad > 30) {
            throw new IllegalArgumentException("Las porciones deben estar entre 1 y 30");
        }
        if (nuevaCantidad < this.porcionesComprometidas) {
            throw new IllegalStateException(
                "No puedes reducir por debajo de las porciones comprometidas: " + this.porcionesComprometidas
            );
        }
        this.porcionesTotales = nuevaCantidad;
        recalcularEstado();
    }

    public boolean estaVigente() {
        return this.fechaExpiracion != null && LocalDateTime.now().isBefore(this.fechaExpiracion);
    }
}