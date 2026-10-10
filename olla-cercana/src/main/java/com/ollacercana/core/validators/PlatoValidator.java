package com.ollacercana.core.validators;

import com.ollacercana.controller.handlers.exception.CantidadAjusteInvalidaException;
import com.ollacercana.controller.handlers.exception.PlatoExpiradoException;
import com.ollacercana.controller.handlers.exception.ReduccionPorDebajoDeComprometidasException;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.TipoAjustePorciones;
import com.ollacercana.core.validators.chain.CocineraHabilitadaHandler;
import com.ollacercana.core.validators.chain.LimitePlatosActivosHandler;
import com.ollacercana.core.validators.chain.PrecioPlatoHandler;
import com.ollacercana.core.validators.chain.ValidadorPlatoHandler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validador de reglas de negocio de Plato.
 *
 * Cadena Chain of Responsibility: CocineraHabilitada → Precio → LimitePlatos.
 *
 * @see OC-091 Validador: cocinera verificada, no pausada, máx 3 activos (RN-28)
 * @see OC-099 Validador con rangos RN-27 (precio, porciones)
 * @see OC-104/105/106/107/108 Ajuste de disponibilidad con lock optimista
 */
@Component
@RequiredArgsConstructor
public class PlatoValidator {

    private final CocineraHabilitadaHandler cocineraHandler;
    private final PrecioPlatoHandler precioHandler;
    private final LimitePlatosActivosHandler limitePlatosHandler;

    private ValidadorPlatoHandler cadena;

    @PostConstruct
    public void inicializarCadena() {
        cocineraHandler.setSiguiente(precioHandler);
        precioHandler.setSiguiente(limitePlatosHandler);
        this.cadena = cocineraHandler;
    }

    public void validarParaPublicar(Plato plato) {
        cadena.validar(plato);
    }

    public void validarAjusteDisponibilidad(Plato plato, TipoAjustePorciones tipo, Integer cantidad) {
        if (plato.getEstado() == EstadoPlato.EXPIRADO) {
            throw new PlatoExpiradoException();
        }
        if (tipo != TipoAjustePorciones.MARCAR_AGOTADO) {
            if (cantidad == null || cantidad <= 0) {
                throw new CantidadAjusteInvalidaException();
            }
        }
        if (tipo == TipoAjustePorciones.DISMINUIR) {
            int comprometidas = plato.getPorcionesComprometidas() == null ? 0 : plato.getPorcionesComprometidas();
            int nuevoTotal = plato.getPorcionesTotales() - cantidad;
            if (nuevoTotal < comprometidas) {
                throw new ReduccionPorDebajoDeComprometidasException(comprometidas);
            }
        }
    }
}