package com.ollacercana.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * OC-87: entidad de persistencia de Plato.
 */
@Entity
@Table(name = "platos")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cocinera_id", nullable = false)
    private UUID cocineraId;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 300)
    private String descripcion;

    @Column(name = "foto_url", nullable = false)
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comida", nullable = false)
    private TipoComida tipoComida;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "plato_restricciones", joinColumns = @JoinColumn(name = "plato_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "restriccion")
    private List<RestriccionAlimentaria> restricciones;

    @Column(name = "porciones_totales", nullable = false)
    private Integer porcionesTotales;

    @Column(name = "porciones_comprometidas")
    private Integer porcionesComprometidas;

    @Column(name = "precio_porcion", nullable = false)
    private BigDecimal precioPorcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPlato estado;

    @Column(name = "hora_disponibilidad")
    private LocalDateTime horaDisponibilidad;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    private Double latitud;
    private Double longitud;

    @Column(name = "punto_entrega")
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
     */
    public void publicar() {
        if (this.cocineraId == null) {
            throw new IllegalStateException("Un plato no puede publicarse sin una cocinera asociada");
        }

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

    public void ajustarDisponibilidad(TipoAjustePorciones tipo, Integer cantidad) {

        int comprometidasSeguras = this.porcionesComprometidas == null ? 0 : this.porcionesComprometidas;

        switch (tipo) {
            case AUMENTAR -> this.porcionesTotales = this.porcionesTotales + cantidad;
            case DISMINUIR -> this.porcionesTotales = this.porcionesTotales - cantidad;
            case MARCAR_AGOTADO -> this.porcionesTotales = comprometidasSeguras;
        }

        recalcularEstado();
        this.version = (this.version == null ? 0 : this.version) + 1;
    }


    public boolean estaVigente() {
        return this.fechaExpiracion != null && LocalDateTime.now().isBefore(this.fechaExpiracion);
    }
}
