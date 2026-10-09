package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.request.EnviarMensajeRequestDTO;
import com.ollacercana.controller.dtos.response.MensajeResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ChatService {

    MensajeResponseDTO enviarMensaje(UUID reservaId, EnviarMensajeRequestDTO request);

    List<MensajeResponseDTO> listarMensajes(UUID reservaId, LocalDateTime desde);

    void marcarLeidos(UUID reservaId);

    long contarNoLeidos(UUID reservaId);
}
