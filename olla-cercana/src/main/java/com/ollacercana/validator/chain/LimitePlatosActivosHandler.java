package com.ollacercana.validator.chain;

import com.ollacercana.exception.LimitePlatosActivosExcedidoException;
import com.ollacercana.model.domain.EstadoPlato;
import com.ollacercana.model.domain.Plato;
import com.ollacercana.repository.PlatoRepository;
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