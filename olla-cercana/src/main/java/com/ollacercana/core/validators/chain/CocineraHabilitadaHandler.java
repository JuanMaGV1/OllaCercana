package com.ollacercana.core.validators.chain;

import com.ollacercana.controller.handlers.exception.CocineraNoEncontradaException;
import com.ollacercana.controller.handlers.exception.CocineraNoVerificadaException;
import com.ollacercana.controller.handlers.exception.CocineraPausadaException;
import com.ollacercana.controller.handlers.exception.PlatoSinCocineraException;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.validators.CocineraQueryPort;

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