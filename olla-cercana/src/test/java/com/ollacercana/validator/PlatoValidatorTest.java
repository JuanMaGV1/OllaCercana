package com.ollacercana.validator;

import com.ollacercana.controller.handlers.exception.CantidadAjusteInvalidaException;
import com.ollacercana.controller.handlers.exception.PlatoExpiradoException;
import com.ollacercana.controller.handlers.exception.ReduccionPorDebajoDeComprometidasException;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.TipoAjustePorciones;
import com.ollacercana.core.validators.PlatoValidator;
import com.ollacercana.core.validators.chain.CocineraHabilitadaHandler;
import com.ollacercana.core.validators.chain.LimitePlatosActivosHandler;
import com.ollacercana.core.validators.chain.PrecioPlatoHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatoValidatorTest {

    @Mock
    private CocineraHabilitadaHandler cocineraHandler;

    @Mock
    private PrecioPlatoHandler precioHandler;

    @Mock
    private LimitePlatosActivosHandler limitePlatosHandler;

    @InjectMocks
    private PlatoValidator validator;

    private Plato plato;

    @BeforeEach
    void setUp() {
        plato = Plato.builder()
                .id(UUID.randomUUID())
                .cocineraId(UUID.randomUUID())
                .nombre("Sancocho")
                .porcionesTotales(5)
                .porcionesComprometidas(2)
                .precioPorcion(new BigDecimal("15000"))
                .estado(EstadoPlato.ACTIVO)
                .build();
    }

    @Test
    @DisplayName("inicializarCadena y validarParaPublicar delega al primer handler de la cadena")
    void validarParaPublicar_ejecutaCadena() {
        validator.inicializarCadena();

        verify(cocineraHandler).setSiguiente(precioHandler);
        verify(precioHandler).setSiguiente(limitePlatosHandler);

        validator.validarParaPublicar(plato);
        verify(cocineraHandler).validar(plato);
    }

    @Test
    @DisplayName("validarAjusteDisponibilidad: Plato expirado lanza PlatoExpiradoException")
    void validarAjuste_platoExpirado_lanzaExcepcion() {
        plato.setEstado(EstadoPlato.EXPIRADO);
        assertThrows(PlatoExpiradoException.class,
                () -> validator.validarAjusteDisponibilidad(plato, TipoAjustePorciones.AUMENTAR, 1));
    }

    @Test
    @DisplayName("validarAjusteDisponibilidad: Aumentar o disminuir con cantidad null o <= 0 lanza CantidadAjusteInvalidaException")
    void validarAjuste_cantidadInvalida_lanzaExcepcion() {
        assertThrows(CantidadAjusteInvalidaException.class,
                () -> validator.validarAjusteDisponibilidad(plato, TipoAjustePorciones.AUMENTAR, null));

        assertThrows(CantidadAjusteInvalidaException.class,
                () -> validator.validarAjusteDisponibilidad(plato, TipoAjustePorciones.AUMENTAR, 0));

        assertThrows(CantidadAjusteInvalidaException.class,
                () -> validator.validarAjusteDisponibilidad(plato, TipoAjustePorciones.DISMINUIR, -3));
    }

    @Test
    @DisplayName("validarAjusteDisponibilidad: MARCAR_AGOTADO permite cantidad null")
    void validarAjuste_marcarAgotado_cantidadNull_valido() {
        assertDoesNotThrow(() -> validator.validarAjusteDisponibilidad(plato, TipoAjustePorciones.MARCAR_AGOTADO, null));
    }

    @Test
    @DisplayName("validarAjusteDisponibilidad: Disminuir por debajo de comprometidas lanza ReduccionPorDebajoDeComprometidasException")
    void validarAjuste_disminuirPorDebajoDeComprometidas_lanzaExcepcion() {
        plato.setPorcionesTotales(5);
        plato.setPorcionesComprometidas(3);

        // Nuevo total sería 5 - 3 = 2 < 3
        assertThrows(ReduccionPorDebajoDeComprometidasException.class,
                () -> validator.validarAjusteDisponibilidad(plato, TipoAjustePorciones.DISMINUIR, 3));
    }

    @Test
    @DisplayName("validarAjusteDisponibilidad: Disminuir con comprometidas null usa 0 como salvaguarda")
    void validarAjuste_disminuirConComprometidasNull_valido() {
        plato.setPorcionesTotales(5);
        plato.setPorcionesComprometidas(null);

        assertDoesNotThrow(() -> validator.validarAjusteDisponibilidad(plato, TipoAjustePorciones.DISMINUIR, 3));
    }

    @Test
    @DisplayName("validarAjusteDisponibilidad: Aumentar o disminuir válido pasa sin excepciones")
    void validarAjuste_valido_pasaExitoso() {
        plato.setPorcionesTotales(5);
        plato.setPorcionesComprometidas(2);

        // Disminuir 2: nuevo total 3 >= 2 comprometidas
        assertDoesNotThrow(() -> validator.validarAjusteDisponibilidad(plato, TipoAjustePorciones.DISMINUIR, 2));

        // Aumentar 3
        assertDoesNotThrow(() -> validator.validarAjusteDisponibilidad(plato, TipoAjustePorciones.AUMENTAR, 3));
    }
}