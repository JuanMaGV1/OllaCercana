package com.ollacercana.validator;

import com.ollacercana.exception.*;
import com.ollacercana.model.domain.EstadoPlato;
import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.domain.TipoAjustePorciones;
import com.ollacercana.model.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.repository.PlatoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlatoValidator {

    private static final int MAX_PLATOS_ACTIVOS = 3;   // RN-28
    private static final BigDecimal PRECIO_MIN = new BigDecimal("2000");
    private static final BigDecimal PRECIO_MAX = new BigDecimal("50000");
    private static final int PORCIONES_MIN = 1;
    private static final int PORCIONES_MAX = 30;
    private static final BigDecimal MULTIPLO = new BigDecimal("100");
    private static final int MAX_RESTRICCIONES = 3;

    private final PlatoRepository platoRepository;
    private final CocineraQueryPort cocineraQueryPort;

    public void validarParaPublicar(Plato plato) {
        validarCocineraHabilitada(plato.getCocineraId());
        validarRangoPrecio(plato.getPrecioPorcion());
        validarRangoPorciones(plato.getPorcionesTotales());
        validarMultiploDe100(plato.getPrecioPorcion());
        validarLimitePlatosActivos(plato.getCocineraId());
        validarRestricciones(plato);
    }

    public void validarParaAjustar(Plato plato) {
        if (plato.getEstado() == EstadoPlato.EXPIRADO) {
            throw new PlatoExpiradoException();
        }
    }

    /**
     * HU-24.
     */
    public void validarAjusteDisponibilidad(Plato plato, AjusteDisponibilidadRequest request) {

        if (request.tipo() != TipoAjustePorciones.MARCAR_AGOTADO) {
            if (request.cantidad() == null || request.cantidad() <= 0) {
                throw new CantidadAjusteInvalidaException();
            }
        }

        if (request.tipo() == TipoAjustePorciones.DISMINUIR) {
            int comprometidas = plato.getPorcionesComprometidas() == null ? 0 : plato.getPorcionesComprometidas();
            int nuevoTotal = plato.getPorcionesTotales() - request.cantidad();
            if (nuevoTotal < comprometidas) {
                throw new ReduccionPorDebajoDeComprometidasException(comprometidas);
            }
        }
    }

    // Validaciones privadas

    private void validarCocineraHabilitada(UUID cocineraId) {
        if (cocineraId == null) {
            throw new PlatoSinCocineraException();
        }
        if (!cocineraQueryPort.estaVerificada(cocineraId)) {
            throw new CocineraNoVerificadaException();
        }
        if (cocineraQueryPort.estaPausada(cocineraId)) {
            throw new CocineraPausadaException();
        }
    }

    private void validarRangoPrecio(BigDecimal precio) {
        if (precio == null) {
            throw new PrecioObligatorioException();
        }
        if (precio.compareTo(PRECIO_MIN) < 0 || precio.compareTo(PRECIO_MAX) > 0) {
            throw new PrecioFueraDeRangoException(PRECIO_MIN, PRECIO_MAX);
        }
    }

    private void validarRangoPorciones(Integer porciones) {
        if (porciones == null || porciones < PORCIONES_MIN || porciones > PORCIONES_MAX) {
            throw new PorcionesFueraDeRangoException(PORCIONES_MIN, PORCIONES_MAX);
        }
    }

    private void validarMultiploDe100(BigDecimal precio) {
        if (precio.remainder(MULTIPLO).compareTo(BigDecimal.ZERO) != 0) {
            throw new PrecioNoMultiploException();
        }
    }

    private void validarLimitePlatosActivos(UUID cocineraId) {
        long activos = platoRepository.countByCocineraIdAndEstadoAndFechaExpiracionAfter(
                cocineraId, EstadoPlato.ACTIVO, LocalDateTime.now());
        if (activos >= MAX_PLATOS_ACTIVOS) {
            throw new LimitePlatosActivosExcedidoException(MAX_PLATOS_ACTIVOS);
        }
    }

    private void validarRestricciones(Plato plato) {
        if (plato.getRestricciones() != null && plato.getRestricciones().size() > MAX_RESTRICCIONES) {
            throw new LimiteRestriccionesExcedidoException(MAX_RESTRICCIONES);
        }
    }
}