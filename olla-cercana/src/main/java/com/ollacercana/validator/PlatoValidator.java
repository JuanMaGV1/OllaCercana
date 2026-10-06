package com.ollacercana.validator;

import com.ollacercana.exception.CantidadAjusteInvalidaException;
import com.ollacercana.exception.PlatoExpiradoException;
import com.ollacercana.exception.ReduccionPorDebajoDeComprometidasException;
import com.ollacercana.model.domain.EstadoPlato;
import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.domain.TipoAjustePorciones;
import com.ollacercana.validator.chain.CocineraHabilitadaHandler;
import com.ollacercana.validator.chain.LimitePlatosActivosHandler;
import com.ollacercana.validator.chain.PrecioPlatoHandler;
import com.ollacercana.validator.chain.ValidadorPlatoHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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