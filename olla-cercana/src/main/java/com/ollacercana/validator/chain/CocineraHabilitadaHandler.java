package com.ollacercana.validator.chain;

import com.ollacercana.domain.Plato;
import com.ollacercana.exception.CocineraNoEncontradaException;
import com.ollacercana.exception.CocineraNoVerificadaException;
import com.ollacercana.exception.CocineraPausadaException;
import com.ollacercana.exception.PlatoSinCocineraException;
import com.ollacercana.validator.CocineraQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CocineraHabilitadaHandler extends AbstractValidadorPlatoHandler {

    private final CocineraQueryPort cocineraQueryPort;

    @Override
    public void validar(Plato plato) {
        UUID cocineraId = plato.getCocineraId();
        if (cocineraId == null) {
            throw new PlatoSinCocineraException();
        }
        if (!cocineraQueryPort.estaVerificada(cocineraId)) {
            throw new CocineraNoVerificadaException();
        }
        if (cocineraQueryPort.estaPausada(cocineraId)) {
            throw new CocineraPausadaException();
        }
        pasarAlSiguiente(plato);
    }
}