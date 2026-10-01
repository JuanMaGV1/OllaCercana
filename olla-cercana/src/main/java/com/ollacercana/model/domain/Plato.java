package com.ollacercana.model.domain;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Plato {

    private UUID id;
    private UUID cocineraId;
    private String nombre;
    private String descripcion;
    private String fotoUrl;
    private TipoComida tipoComida;
    private List<RestriccionAlimentaria> restricciones;
    private Integer porcionesTotales;
    private Integer porcionesComprometidas;
    private BigDecimal precioPorcion;
    private EstadoPlato estado;
    private LocalDateTime horaDisponibilidad;
    private LocalDateTime fechaPublicacion;
    private LocalDateTime fechaExpiracion;
    private Double latitud;
    private Double longitud;
    private String puntoEntrega;

    // ============ Reglas de negocio ============

    public int getPorcionesDisponibles() {
        int comprometidas = porcionesComprometidas == null ? 0 : porcionesComprometidas;
        return porcionesTotales - comprometidas;
    }

    public void publicar() {
        if (this.cocineraId == null) {
            throw new IllegalStateException("Un plato no puede publicarse sin una cocinera asociada");
        }
        this.id = UUID.randomUUID();
        this.estado = EstadoPlato.ACTIVO;
        this.porcionesComprometidas = 0;
        this.fechaPublicacion = LocalDateTime.now();
        this.fechaExpiracion = this.fechaPublicacion.plusHours(4);
    }

    public void marcarAgotado() {
        this.estado = EstadoPlato.AGOTADO;
    }

    public void expirar() {
        this.estado = EstadoPlato.EXPIRADO;
    }

    public void recalcularEstado() {
        if (this.estado == EstadoPlato.EXPIRADO) return;
        if (getPorcionesDisponibles() <= 0) {
            this.estado = EstadoPlato.AGOTADO;
        } else if (this.estado == EstadoPlato.AGOTADO) {
            this.estado = EstadoPlato.ACTIVO;
        }
    }

    public void comprometerPorciones(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (cantidad > getPorcionesDisponibles()) {
            throw new IllegalStateException(
                    "No hay suficientes porciones disponibles. Disponibles: " + getPorcionesDisponibles()
            );
        }
        this.porcionesComprometidas = (porcionesComprometidas == null ? 0 : porcionesComprometidas) + cantidad;
        recalcularEstado();
    }

    public void liberarPorciones(int cantidad) {
        int comprometidas = porcionesComprometidas == null ? 0 : porcionesComprometidas;
        this.porcionesComprometidas = Math.max(0, comprometidas - cantidad);
        recalcularEstado();
    }

    public void cambiarPorcionesTotales(int nuevaCantidad) {
        if (nuevaCantidad < 1 || nuevaCantidad > 30) {
            throw new IllegalArgumentException("Las porciones deben estar entre 1 y 30");
        }
        int comprometidas = porcionesComprometidas == null ? 0 : porcionesComprometidas;
        if (nuevaCantidad < comprometidas) {
            throw new IllegalStateException(
                    "No puedes reducir por debajo de las porciones comprometidas: " + comprometidas
            );
        }
        this.porcionesTotales = nuevaCantidad;
        recalcularEstado();
    }

    public void ajustarDisponibilidad(TipoAjustePorciones tipo, Integer cantidad) {
        int comprometidasSeguras = this.porcionesComprometidas == null ? 0 : this.porcionesComprometidas;

        switch (tipo) {
            case AUMENTAR -> this.porcionesTotales = this.porcionesTotales + cantidad;
            case DISMINUIR -> this.porcionesTotales = this.porcionesTotales - cantidad;
            case MARCAR_AGOTADO -> this.porcionesTotales = comprometidasSeguras;
        }
        recalcularEstado();
    }

    public boolean estaVigente() {
        return this.fechaExpiracion != null && LocalDateTime.now().isBefore(this.fechaExpiracion);
    }
}