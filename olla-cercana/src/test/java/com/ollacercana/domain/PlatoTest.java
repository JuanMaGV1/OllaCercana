package com.ollacercana.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PlatoTest {

    private Plato crearPlato() {
        return Plato.builder()
            .nombre("Arroz con pollo")
            .descripcion("Arroz con pollo criollo")
            .fotoUrl("http://foto.com")
            .tipoComida(TipoComida.ALMUERZO)
            .porcionesTotales(10)
            .precioPorcion(new BigDecimal("12000.00"))
            .puntoEntrega("Portería")
            .build();
    }

    @Test
    void publicar_debeInicializarCampos() {
        Plato plato = crearPlato();
        plato.publicar();

        assertEquals(EstadoPlato.ACTIVO, plato.getEstado());
        assertEquals(0, plato.getPorcionesComprometidas());
        assertNotNull(plato.getFechaPublicacion());
        assertEquals(plato.getFechaPublicacion().plusHours(4), plato.getFechaExpiracion());
    }

    @Test
    void comprometerPorciones_debeMarcarAgotado() {
        Plato plato = crearPlato();
        plato.publicar();
        plato.comprometerPorciones(10);

        assertEquals(0, plato.getPorcionesDisponibles());
        assertEquals(EstadoPlato.AGOTADO, plato.getEstado());
    }

    @Test
    void comprometerPorciones_debeFallarSiNoHaySuficientes() {
        Plato plato = crearPlato();
        plato.publicar();

        assertThrows(IllegalStateException.class, () -> plato.comprometerPorciones(20));
    }

    @Test
    void liberarPorciones_debeReactivar() {
        Plato plato = crearPlato();
        plato.publicar();
        plato.comprometerPorciones(10);
        plato.liberarPorciones(5);

        assertEquals(5, plato.getPorcionesDisponibles());
        assertEquals(EstadoPlato.ACTIVO, plato.getEstado());
    }

    @Test
    void cambiarPorcionesTotales_debeValidarRango() {
        Plato plato = crearPlato();
        plato.publicar();

        assertThrows(IllegalArgumentException.class, () -> plato.cambiarPorcionesTotales(0));
        assertThrows(IllegalArgumentException.class, () -> plato.cambiarPorcionesTotales(50));
    }

    @Test
    void cambiarPorcionesTotales_debeFallarSiEsMenorALasComprometidas() {
        Plato plato = crearPlato();
        plato.publicar();
        plato.comprometerPorciones(7);

        assertThrows(IllegalStateException.class, () -> plato.cambiarPorcionesTotales(5));
    }
}