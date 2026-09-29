package com.ollacercana.validator;

import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.domain.Plato;
import com.ollacercana.exception.AutoReservaException;
import com.ollacercana.exception.LimiteReservasPendientesException;
import com.ollacercana.exception.PorcionesInsuficientesException;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReservaValidator {

    private static final int MAX_RESERVAS_PENDIENTES = 2; // RN-15

    private final ReservaRepository reservaRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;

    public void validarParaCrear(Long compradorId, Plato plato, int cantidadPorciones) {
        // (1) La cocinera no reserva su propio plato (RN-14)
        // Verificamos si la cuenta del comprador tiene un perfil de cocinera que coincida con el plato
        Optional<PerfilCocinera> perfilComprador = perfilCocineraRepository.findByCuentaId(compradorId);
        if (perfilComprador.isPresent() && perfilComprador.get().getId().equals(plato.getCocineraId())) {
            throw new AutoReservaException();
        }

        // (2) El comprador tiene menos de 2 reservas Pendientes (RN-15)
        long pendientes = reservaRepository.countByCompradorIdAndEstado(compradorId, EstadoReserva.PENDIENTE);
        if (pendientes >= MAX_RESERVAS_PENDIENTES) {
            throw new LimiteReservasPendientesException();
        }

        // (3) Hay porciones disponibles (RN-03)
        if (plato.getPorcionesDisponibles() < cantidadPorciones) {
            throw new PorcionesInsuficientesException(plato.getPorcionesDisponibles());
        }
    }
}