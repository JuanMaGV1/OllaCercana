package com.ollacercana.core.validators;

import com.ollacercana.controller.handlers.exception.AutoReservaException;
import com.ollacercana.controller.handlers.exception.LimiteReservasPendientesException;
import com.ollacercana.controller.handlers.exception.MedioPagoNoAceptadoException;
import com.ollacercana.controller.handlers.exception.PorcionesInsuficientesException;
import com.ollacercana.controller.handlers.exception.ReglaDeNegocioException;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.models.enums.MedioPago;
import com.ollacercana.controller.handlers.exception.MedioPagoNoAceptadoException;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReservaValidator {

    private static final int MAX_RESERVAS_PENDIENTES = 2;

    private final ReservaRepository reservaRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;

    public void validarParaCrear(Long compradorId, Plato plato, int cantidadPorciones) {
        validarParaCrear(compradorId, plato, cantidadPorciones, null);
    }

    public void validarParaCrear(Long compradorId, Plato plato, int cantidadPorciones, MedioPago medioPago) {

        if (plato.getEstado() != EstadoPlato.ACTIVO) {
            throw new ReglaDeNegocioException("El plato no está disponible para reservar");
        }
        if (!plato.estaVigente()) {
            throw new ReglaDeNegocioException("El plato no está disponible para reservar");
        }

        Optional<PerfilCocineraEntity> perfilComprador =
                perfilCocineraRepository.findByCuentaId(compradorId);
        if (perfilComprador.isPresent()
                && perfilComprador.get().getId().equals(plato.getCocineraId())) {
            throw new AutoReservaException();
        }

        long pendientes = reservaRepository
                .countByCompradorIdAndEstado(compradorId, EstadoReserva.PENDIENTE);
        if (pendientes >= MAX_RESERVAS_PENDIENTES) {
            throw new LimiteReservasPendientesException();
        }

        if (plato.getPorcionesDisponibles() < cantidadPorciones) {
            throw new PorcionesInsuficientesException(plato.getPorcionesDisponibles());
        }

        // OC-255: el medio de pago es opcional; si se indica, debe ser aceptado por la cocinera
        if (medioPago != null) {
            boolean aceptado = perfilCocineraRepository.findById(plato.getCocineraId())
                    .map(PerfilCocineraEntity::getMediosPago)
                    .map(medios -> medios.contains(medioPago))
                    .orElse(false);
            if (!aceptado) {
                throw new MedioPagoNoAceptadoException(medioPago);
            }
        }
    }
}