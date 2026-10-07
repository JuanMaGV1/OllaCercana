package com.ollacercana.validator;

import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.domain.Plato;
import com.ollacercana.exception.AutoReservaException;
import com.ollacercana.exception.LimiteReservasPendientesException;
import com.ollacercana.exception.PorcionesInsuficientesException;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.ReservaRepository;
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
        if (plato.getEstado() != EstadoPlato.ACTIVO || !plato.estaVigente()) {
            throw new ReglaDeNegocioException("El plato no está disponible para reservar");
        }

                                                             
                                                                                                       
        Optional<PerfilCocinera> perfilComprador = perfilCocineraRepository.findByCuentaId(compradorId);
        if (perfilComprador.isPresent() && perfilComprador.get().getId().equals(plato.getCocineraId())) {
            throw new AutoReservaException();
        }

                                                                        
        long pendientes = reservaRepository.countByCompradorIdAndEstado(compradorId, EstadoReserva.PENDIENTE);
        if (pendientes >= MAX_RESERVAS_PENDIENTES) {
            throw new LimiteReservasPendientesException();
        }

                                                
        if (plato.getPorcionesDisponibles() < cantidadPorciones) {
            throw new PorcionesInsuficientesException(plato.getPorcionesDisponibles());
        }
    }
}
