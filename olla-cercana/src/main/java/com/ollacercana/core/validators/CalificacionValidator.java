package com.ollacercana.core.validators;

import com.ollacercana.controller.handlers.exception.*;
import com.ollacercana.core.models.Reserva;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.persistence.repository.CalificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CalificacionValidator {

    public static final int HORAS_VENTANA = 72;

    private final CalificacionRepository calificacionRepository;

    public void validarParaCalificar(Reserva reserva, Long compradorId, Integer estrellas) {
        if (!reserva.getCompradorId().equals(compradorId)) {
            throw new AccesoDenegadoCalificacionException();
        }
        if (reserva.getEstado() != EstadoReserva.COMPLETADA) {
            throw new ReservaNoCompletadaException(reserva.getEstado());
        }
        if (calificacionRepository.findByReservaId(reserva.getId()).isPresent()) {
            throw new CalificacionDuplicadaException();
        }
        if (estrellas == null || estrellas < 1 || estrellas > 5) {
            throw new ReglaDeNegocioException("Debe seleccionar al menos una estrella");
        }
    }

    public LocalDateTime calcularFechaLimite(LocalDateTime ahora) {
        return ahora.plusHours(HORAS_VENTANA);
    }
}