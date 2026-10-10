package com.ollacercana.controller;

import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.docs.NotificacionApi;
import com.ollacercana.controller.dtos.response.NotificacionResponseDTO;
import com.ollacercana.core.services.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
public class NotificacionController implements NotificacionApi {

    private final NotificacionService notificacionService;
    private final UsuarioActual usuarioActual;

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificacionResponseDTO>> listar() {
        return ResponseEntity.ok(notificacionService.listarParaUsuario(usuarioActual.getCuentaId()));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> marcarLeida(@PathVariable String id) {
        notificacionService.marcarLeida(id, usuarioActual.getCuentaId());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> contarNoLeidas() {
        return ResponseEntity.ok(notificacionService.contarNoLeidas(usuarioActual.getCuentaId()));
    }
}