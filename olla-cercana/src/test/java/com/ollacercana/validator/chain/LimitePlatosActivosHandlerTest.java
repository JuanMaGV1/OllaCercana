package com.ollacercana.validator.chain;

import com.ollacercana.controller.handlers.exception.LimitePlatosActivosExcedidoException;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.validators.chain.LimitePlatosActivosHandler;
import com.ollacercana.core.validators.chain.ValidadorPlatoHandler;
import com.ollacercana.persistence.repository.PlatoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LimitePlatosActivosHandlerTest {

    @Mock
    private PlatoRepository platoRepository;

    @Mock
    private ValidadorPlatoHandler siguienteHandler;

    @InjectMocks
    private LimitePlatosActivosHandler handler;

    private Plato plato;
    private final UUID cocineraId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        plato = Plato.builder().cocineraId(cocineraId).build();
    }

    @Test
    @DisplayName("Límite de 3 platos activos alcanzado lanza LimitePlatosActivosExcedidoException")
    void validar_limiteAlcanzado_lanzaExcepcion() {
        when(platoRepository.countByCocineraIdAndEstadoAndFechaExpiracionAfter(
                eq(cocineraId), eq(EstadoPlato.ACTIVO), any(LocalDateTime.class))).thenReturn(3L);

        assertThrows(LimitePlatosActivosExcedidoException.class, () -> handler.validar(plato));
        verify(siguienteHandler, never()).validar(any());
    }

    @Test
    @DisplayName("Menos de 3 platos activos pasa al siguiente handler")
    void validar_dentroDelLimite_pasaAlSiguiente() {
        when(platoRepository.countByCocineraIdAndEstadoAndFechaExpiracionAfter(
                eq(cocineraId), eq(EstadoPlato.ACTIVO), any(LocalDateTime.class))).thenReturn(2L);
        handler.setSiguiente(siguienteHandler);

        handler.validar(plato);

        verify(siguienteHandler).validar(plato);
    }

    @Test
    @DisplayName("Sin siguiente handler y dentro del límite pasa exitosamente")
    void validar_sinSiguienteHandler_exitoso() {
        when(platoRepository.countByCocineraIdAndEstadoAndFechaExpiracionAfter(
                eq(cocineraId), eq(EstadoPlato.ACTIVO), any(LocalDateTime.class))).thenReturn(0L);

        assertDoesNotThrow(() -> handler.validar(plato));
    }
}