package com.ollacercana.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlatoTest {

    private Plato plato;

    @BeforeEach
    void setUp() {
        plato = Plato.builder()
                .id(UUID.randomUUID())
                .cocineraId(UUID.randomUUID())
                .nombre("Bandeja paisa")
                .porcionesTotales(5)
                .porcionesComprometidas(0)
                .precioPorcion(new BigDecimal("12000"))
                .estado(EstadoPlato.ACTIVO)
                .build();
    }

    @Test
    void disponibles_debeSerTotalesMenosComprometidas() {
        plato.setPorcionesComprometidas(2);
        assertThat(plato.getPorcionesDisponibles()).isEqualTo(3);
    }

    @Test
    void disponibles_debeSerTotalesCuandoComprometidasEsNull() {
        plato.setPorcionesComprometidas(null);
        assertThat(plato.getPorcionesDisponibles()).isEqualTo(5);
    }

    @Test
    void disponibles_debeSerCeroCuandoTodasEstanComprometidas() {
        plato.setPorcionesComprometidas(5);
        assertThat(plato.getPorcionesDisponibles()).isEqualTo(0);
    }

    @Test
    void comprometerPorciones_debeDescontarDeDisponibles() {
        plato.comprometerPorciones(3);
        assertThat(plato.getPorcionesComprometidas()).isEqualTo(3);
        assertThat(plato.getPorcionesDisponibles()).isEqualTo(2);
    }

    @Test
    void comprometerPorciones_debeMarcarAgotadoCuandoLlegaACero() {
        plato.comprometerPorciones(5);
        assertThat(plato.getPorcionesDisponibles()).isEqualTo(0);
        assertThat(plato.getEstado()).isEqualTo(EstadoPlato.AGOTADO);
    }

    @Test
    void comprometerPorciones_debeLanzarExcepcionSiExcedeDisponibles() {
        assertThatThrownBy(() -> plato.comprometerPorciones(6))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No hay suficientes porciones disponibles");
    }

    @Test
    void comprometerPorciones_debeLanzarExcepcionSiCantidadEsCeroONegativa() {
        assertThatThrownBy(() -> plato.comprometerPorciones(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void liberarPorciones_debeAumentarDisponibles() {
        plato.comprometerPorciones(4);
        plato.liberarPorciones(2);
        assertThat(plato.getPorcionesComprometidas()).isEqualTo(2);
        assertThat(plato.getPorcionesDisponibles()).isEqualTo(3);
    }

    @Test
    void liberarPorciones_debeReactivarPlatoAgotado() {
        plato.setEstado(EstadoPlato.AGOTADO);
        plato.setPorcionesComprometidas(5);
        plato.liberarPorciones(2);
        assertThat(plato.getEstado()).isEqualTo(EstadoPlato.ACTIVO);
    }

    @Test
    void liberarPorciones_noDebeQuedarNegativo() {
        plato.comprometerPorciones(2);
        plato.liberarPorciones(10);
        assertThat(plato.getPorcionesComprometidas()).isEqualTo(0);
    }
}