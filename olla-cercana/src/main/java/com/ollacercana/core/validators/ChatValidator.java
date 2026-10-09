package com.ollacercana.core.validators;

import com.ollacercana.controller.handlers.exception.AccesoDenegadoException;
import com.ollacercana.controller.handlers.exception.ConflictoException;
import com.ollacercana.controller.handlers.exception.ReservaNoEncontradaException;
import com.ollacercana.core.models.enums.EstadoChat;
import com.ollacercana.persistence.entities.ReservaEntity;
import com.ollacercana.persistence.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatValidator {

    private final ReservaRepository reservaRepository;

    public ReservaEntity validarParaEnviarMensaje(UUID reservaId, Long cuentaId, UUID cocineraId, String texto) {
        ReservaEntity reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNoEncontradaException(reservaId));

        validarParticipante(reserva, cuentaId, cocineraId);
        validarChatActivo(reserva);
        validarTexto(texto);

        return reserva;
    }

    public ReservaEntity validarAccesoChat(UUID reservaId, Long cuentaId, UUID cocineraId) {
        ReservaEntity reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNoEncontradaException(reservaId));

        validarParticipante(reserva, cuentaId, cocineraId);
        return reserva;
    }

    public void validarParticipante(ReservaEntity reserva, Long cuentaId, UUID cocineraId) {
        boolean esComprador = reserva.getCompradorId() != null && reserva.getCompradorId().equals(cuentaId);
        boolean esCocinera = cocineraId != null && reserva.getCocineraId() != null && reserva.getCocineraId().equals(cocineraId);

        if (!esComprador && !esCocinera) {
            throw new AccesoDenegadoException("No tienes permiso sobre este chat");
        }
    }

    public void validarChatActivo(ReservaEntity reserva) {
        if (reserva.getEstadoChat() != EstadoChat.ACTIVO) {
            throw new ConflictoException("Chat finalizado");
        }
    }

    public void validarTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El texto del mensaje no puede estar vacío");
        }
        if (texto.length() > 500) {
            throw new IllegalArgumentException("El texto no puede superar los 500 caracteres");
        }
    }
}
