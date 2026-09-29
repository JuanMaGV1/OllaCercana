package com.ollacercana.service;

import com.ollacercana.domain.*;
import com.ollacercana.dto.response.ReservaResponseDTO;
import com.ollacercana.exception.ConflictoException;
import com.ollacercana.exception.PlatoNoEncontradoException;
import com.ollacercana.mapper.ReservaMapper;
import com.ollacercana.repository.EventoReservaRepository;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReservaRepository;
import com.ollacercana.validator.ReservaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements IReservaService {

    private final ReservaRepository reservaRepository;
    private final PlatoRepository platoRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;
    private final EventoReservaRepository eventoReservaRepository;
    private final ReservaValidator validator;
    private final ReservaMapper reservaMapper;

    @Override
    @Transactional
    public ReservaResponseDTO crear(Long compradorId, Reserva reserva) {
        // 1. Obtener plato
        Plato plato = platoRepository.findById(reserva.getPlatoId())
                .orElseThrow(() -> new PlatoNoEncontradoException(reserva.getPlatoId()));

        // 2. Validar reglas RN-14, RN-15, RN-03
        validator.validarParaCrear(compradorId, plato, reserva.getCantidadPorciones());

        // 3. Descontar porciones del plato (RN-03)
        plato.comprometerPorciones(reserva.getCantidadPorciones());

        // 4. Guardar cambios en el plato con bloqueo optimista (@Version)
        try {
            platoRepository.saveAndFlush(plato);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConflictoException("El plato fue modificado por otra transacción simultánea, intenta de nuevo");
        }

        // 5. Calcular monto total (RN-33) y hora límite (+10 min)
        BigDecimal montoTotal = plato.getPrecioPorcion().multiply(BigDecimal.valueOf(reserva.getCantidadPorciones()));
        LocalDateTime ahora = LocalDateTime.now();

        reserva.setCompradorId(compradorId);
        reserva.setMontoTotal(montoTotal);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setFechaCreacion(ahora);
        reserva.setFechaLimiteConfirmacion(ahora.plusMinutes(10));

        // 6. Persistir reserva
        Reserva guardada = reservaRepository.save(reserva);

        // 7. Evento para notificaciones (Observer)
        EventoReserva evento = EventoReserva.builder()
                .tipo(TipoEvento.RESERVA_CREADA)
                .reservaId(guardada.getId())
                .platoId(plato.getId())
                .timestamp(ahora)
                .payload("Reserva creada por " + reserva.getCantidadPorciones() + " porciones. Monto: " + montoTotal)
                .build();
        eventoReservaRepository.save(evento);

        // 8. Conjunto residencial de la cocinera
        String conjunto = perfilCocineraRepository.findById(plato.getCocineraId())
                .map(PerfilCocinera::getConjuntoResidencial)
                .orElse("Conjunto Residencial");

        return reservaMapper.toResponseDTO(guardada, plato.getNombre(), conjunto);
    }
}