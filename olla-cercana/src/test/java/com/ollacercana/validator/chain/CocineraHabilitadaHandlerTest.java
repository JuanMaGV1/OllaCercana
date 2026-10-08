package com.ollacercana.validator.chain;

import com.ollacercana.controller.handlers.exception.CocineraNoVerificadaException;
import com.ollacercana.controller.handlers.exception.CocineraPausadaException;
import com.ollacercana.controller.handlers.exception.PlatoSinCocineraException;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.validators.CocineraQueryPort;
import com.ollacercana.core.validators.chain.CocineraHabilitadaHandler;
import com.ollacercana.core.validators.chain.ValidadorPlatoHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CocineraHabilitadaHandlerTest {

    @Mock
    private CocineraQueryPort cocineraQueryPort;

    @Mock
    private ValidadorPlatoHandler siguienteHandler;

    @InjectMocks
    private CocineraHabilitadaHandler handler;

    private Plato plato;
    private final UUID cocineraId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        plato = Plato.builder().cocineraId(cocineraId).build();
    }

    @Test
    @DisplayName("CocineraId null lanza PlatoSinCocineraException")
    void validar_sinCocinera_lanzaExcepcion() {
        plato.setCocineraId(null);
        assertThrows(PlatoSinCocineraException.class, () -> handler.validar(plato));
    }

    @Test
    @DisplayName("Cocinera no verificada lanza CocineraNoVerificadaException")
    void validar_noVerificada_lanzaExcepcion() {
        when(cocineraQueryPort.estaVerificada(cocineraId)).thenReturn(false);
        assertThrows(CocineraNoVerificadaException.class, () -> handler.validar(plato));
    }

    @Test
    @DisplayName("Cocinera pausada lanza CocineraPausadaException")
    void validar_pausada_lanzaExcepcion() {
        when(cocineraQueryPort.estaVerificada(cocineraId)).thenReturn(true);
        when(cocineraQueryPort.estaPausada(cocineraId)).thenReturn(true);

        assertThrows(CocineraPausadaException.class, () -> handler.validar(plato));
    }

    @Test
    @DisplayName("Cocinera habilitada invoca al siguiente handler")
    void validar_habilitada_pasaAlSiguiente() {
        when(cocineraQueryPort.estaVerificada(cocineraId)).thenReturn(true);
        when(cocineraQueryPort.estaPausada(cocineraId)).thenReturn(false);
        handler.setSiguiente(siguienteHandler);

        handler.validar(plato);

        verify(siguienteHandler).validar(plato);
    }

    @Test
    @DisplayName("Cocinera habilitada sin siguiente handler finaliza correctamente")
    void validar_habilitada_sinSiguiente_exitoso() {
        when(cocineraQueryPort.estaVerificada(cocineraId)).thenReturn(true);
        when(cocineraQueryPort.estaPausada(cocineraId)).thenReturn(false);

        assertDoesNotThrow(() -> handler.validar(plato));
    }
}