package com.ollacercana.validator;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.Plato;
import com.ollacercana.exception.BusinessRuleException;
import com.ollacercana.repository.PlatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PlatoValidator {

    private static final int MAX_PLATOS_ACTIVOS = 3;   // RN-28
    private static final BigDecimal PRECIO_MIN = new BigDecimal("2000");
    private static final BigDecimal PRECIO_MAX = new BigDecimal("50000");
    private static final int PORCIONES_MIN = 1;
    private static final int PORCIONES_MAX = 30;
    private static final BigDecimal MULTIPLO = new BigDecimal("100");

    private final PlatoRepository platoRepository;
    private final CocineraQueryPort cocineraQueryPort;

    public void validarParaPublicar(Plato plato) {
        validarCocineraHabilitada(plato.getCocineraId()); //Escenario 2 del HU-04
        validarRangoPrecio(plato.getPrecioPorcion());       // RN-27
        validarRangoPorciones(plato.getPorcionesTotales()); // RN-27
        validarMultiploDe100(plato.getPrecioPorcion());     // RN-27
        validarLimitePlatosActivos(plato.getCocineraId());  // RN-28
        validarRestricciones(plato);                         // RN-30
    }

    public void validarParaAjustar(Plato plato) {
        if (plato.getEstado() == EstadoPlato.EXPIRADO) {
            throw new BusinessRuleException("Un plato expirado no se puede modificar (RN-29)");
        }
    }

    // ============ Validaciones privadas ============

    private void validarCocineraHabilitada(java.util.UUID cocineraId) {
        if (cocineraId == null) {
            throw new BusinessRuleException("El plato debe tener una cocinera asociada.")
        }

        if (CocineraQueryPort.estaVerificada(cocinaraId)){
            throw new BusinessRuleException("Debes verificar tu telefono antes de publicar un plato")
        }

        if (CocineraQueryPort.estaPausada(cocinaraId)){
            throw new BusinessRuleException("Tu perfil esta pausado, no puedes publicar platos.")
        }
    }



    private void validarRangoPrecio(BigDecimal precio) {
        if (precio == null) {
            throw new BusinessRuleException("El precio es obligatorio");
        }
        if (precio.compareTo(PRECIO_MIN) < 0 || precio.compareTo(PRECIO_MAX) > 0) {
            throw new BusinessRuleException(
                "El precio debe estar entre $" + PRECIO_MIN + " y $" + PRECIO_MAX + " (RN-27)"
            );
        }
    }

    private void validarRangoPorciones(Integer porciones) {
        if (porciones == null || porciones < PORCIONES_MIN || porciones > PORCIONES_MAX) {
            throw new BusinessRuleException(
                "Las porciones deben estar entre " + PORCIONES_MIN + " y " + PORCIONES_MAX + " (RN-27)"
            );
        }
    }

    private void validarMultiploDe100(BigDecimal precio) {
        if (precio.remainder(MULTIPLO).compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessRuleException("El precio debe ser múltiplo de 100 (RN-27)");
        }
    }

    private void validarLimitePlatosActivos() {
        long activos = platoRepository.countActivosVigentes(EstadoPlato.ACTIVO, LocalDateTime.now());
        if (activos >= MAX_PLATOS_ACTIVOS) {
            throw new BusinessRuleException(
                "Máximo " + MAX_PLATOS_ACTIVOS + " platos activos al mismo tiempo (RN-28)"
            );
        }
    }

    private void validarRestricciones(Plato plato) {
        if (plato.getRestricciones() != null && plato.getRestricciones().size() > 3) {
            throw new BusinessRuleException("Máximo 3 restricciones alimentarias (RN-30)");
        }
    }
}