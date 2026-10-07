package com.ollacercana.core.validators.chain;

import com.ollacercana.controller.handlers.exception.LimitePlatosActivosExcedidoException;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.persistence.repository.PlatoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LimitePlatosActivosHandler extends AbstractValidadorPlatoHandler {

    private static final int MAX_PLATOS_ACTIVOS = 3;
    private final PlatoRepository platoRepository;

    @Override
    public void validar(Plato plato) {
        long activos = platoRepository.countByCocineraIdAndEstadoAndFechaExpiracionAfter(
                plato.getCocineraId(), EstadoPlato.ACTIVO, LocalDateTime.now());
        if (activos >= MAX_PLATOS_ACTIVOS) {
            throw new LimitePlatosActivosExcedidoException(MAX_PLATOS_ACTIVOS);
        }
        pasarAlSiguiente(plato);
    }
}