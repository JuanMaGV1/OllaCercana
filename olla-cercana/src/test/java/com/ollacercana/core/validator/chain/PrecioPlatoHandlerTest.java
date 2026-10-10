package com.ollacercana.core.validator.chain;

import com.ollacercana.controller.handlers.exception.PrecioFueraDeRangoException;
import com.ollacercana.controller.handlers.exception.PrecioNoMultiploException;
import com.ollacercana.controller.handlers.exception.PrecioObligatorioException;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.validators.chain.PrecioPlatoHandler;
import com.ollacercana.core.validators.chain.ValidadorPlatoHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PrecioPlatoHandlerTest {

    @Mock
    private ValidadorPlatoHandler siguienteHandler;

    private PrecioPlatoHandler handler;
    private Plato plato;

    @BeforeEach
    void setUp() {
        handler = new PrecioPlatoHandler();
        plato = new Plato();
    }

    @Test
    @DisplayName("Precio null lanza PrecioObligatorioException")
    void validar_precioNull_lanzaExcepcion() {
        plato.setPrecioPorcion(null);
        assertThrows(PrecioObligatorioException.class, () -> handler.validar(plato));
    }

    @Test
    @DisplayName("Precio menor a 2000 lanza PrecioFueraDeRangoException")
    void validar_precioMenorAlMinimo_lanzaExcepcion() {
        plato.setPrecioPorcion(new BigDecimal("1900"));
        assertThrows(PrecioFueraDeRangoException.class, () -> handler.validar(plato));
    }

    @Test
    @DisplayName("Precio mayor a 50000 lanza PrecioFueraDeRangoException")
    void validar_precioMayorAlMaximo_lanzaExcepcion() {
        plato.setPrecioPorcion(new BigDecimal("50100"));
        assertThrows(PrecioFueraDeRangoException.class, () -> handler.validar(plato));
    }

    @Test
    @DisplayName("Precio no múltiplo de 100 lanza PrecioNoMultiploException")
    void validar_precioNoMultiplo_lanzaExcepcion() {
        plato.setPrecioPorcion(new BigDecimal("12550"));
        assertThrows(PrecioNoMultiploException.class, () -> handler.validar(plato));
    }

    @Test
    @DisplayName("Precio válido invoca al siguiente handler")
    void validar_precioValido_invocaSiguiente() {
        plato.setPrecioPorcion(new BigDecimal("15000"));
        handler.setSiguiente(siguienteHandler);

        handler.validar(plato);

        verify(siguienteHandler).validar(plato);
    }

    @Test
    @DisplayName("Precio límite 2000 y 50000 sin siguiente handler pasa sin error")
    void validar_preciosLimites_exitoso() {
        plato.setPrecioPorcion(new BigDecimal("2000"));
        assertDoesNotThrow(() -> handler.validar(plato));

        plato.setPrecioPorcion(new BigDecimal("50000"));
        assertDoesNotThrow(() -> handler.validar(plato));
    }
}