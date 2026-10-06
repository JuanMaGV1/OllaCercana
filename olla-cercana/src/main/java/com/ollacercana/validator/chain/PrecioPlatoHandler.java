package com.ollacercana.validator.chain;

import com.ollacercana.exception.PrecioFueraDeRangoException;
import com.ollacercana.exception.PrecioNoMultiploException;
import com.ollacercana.exception.PrecioObligatorioException;
import com.ollacercana.model.domain.Plato;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PrecioPlatoHandler extends AbstractValidadorPlatoHandler {

    private static final BigDecimal PRECIO_MIN = new BigDecimal("2000");
    private static final BigDecimal PRECIO_MAX = new BigDecimal("50000");
    private static final BigDecimal MULTIPLO = new BigDecimal("100");

    @Override
    public void validar(Plato plato) {
        BigDecimal precio = plato.getPrecioPorcion();
        if (precio == null) {
            throw new PrecioObligatorioException();
        }
        if (precio.compareTo(PRECIO_MIN) < 0 || precio.compareTo(PRECIO_MAX) > 0) {
            throw new PrecioFueraDeRangoException(PRECIO_MIN, PRECIO_MAX);
        }
        if (precio.remainder(MULTIPLO).compareTo(BigDecimal.ZERO) != 0) {
            throw new PrecioNoMultiploException();
        }
        pasarAlSiguiente(plato);
    }
}