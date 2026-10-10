package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.response.NotificacionResponseDTO;
import com.ollacercana.core.models.Notificacion;

import java.util.List;

public interface NotificacionService {

    List<NotificacionResponseDTO> listarParaUsuario(Long cuentaId);

    void marcarLeida(String notificacionId, Long cuentaId);

    long contarNoLeidas(Long cuentaId);

    void registrar(Notificacion notificacion);
}