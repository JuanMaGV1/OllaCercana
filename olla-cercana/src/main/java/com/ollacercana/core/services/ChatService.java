package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.request.EnviarMensajeRequestDTO;
import com.ollacercana.controller.dtos.response.MensajeResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ChatService {

    MensajeResponseDTO enviar(UUID reservaId, EnviarMensajeRequestDTO request);

    default MensajeResponseDTO enviarMensaje(UUID reservaId, EnviarMensajeRequestDTO request) {
        return enviar(reservaId, request);
    }

    List<MensajeResponseDTO> listar(UUID reservaId, LocalDateTime desde);

    default List<MensajeResponseDTO> listarMensajes(UUID reservaId, LocalDateTime desde) {
        return listar(reservaId, desde);
    }

    void marcarLeidos(UUID reservaId);

    long contarNoLeidos(UUID reservaId);

    long purgarMensajesAntiguos();

    long purgarMensajesDeReservasCerradas(LocalDateTime limite);
}