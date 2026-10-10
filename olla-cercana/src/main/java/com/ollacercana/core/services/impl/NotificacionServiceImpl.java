package com.ollacercana.core.services.impl;

import com.ollacercana.controller.dtos.response.NotificacionResponseDTO;
import com.ollacercana.controller.handlers.exception.AccesoDenegadoException;
import com.ollacercana.controller.handlers.exception.ResourceNotFoundException;
import com.ollacercana.core.models.Notificacion;
import com.ollacercana.core.services.NotificacionService;
import com.ollacercana.persistence.document.NotificacionDocument;
import com.ollacercana.persistence.mappers.NotificacionDocumentMapper;
import com.ollacercana.persistence.repository.mongo.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final NotificacionDocumentMapper notificacionMapper;

    @Override
    public List<NotificacionResponseDTO> listarParaUsuario(Long cuentaId) {
        return notificacionRepository
                .findByCompradorIdOrCocineraIdOrderByFechaCreacionDesc(cuentaId, cuentaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void marcarLeida(String notificacionId, Long cuentaId) {
        NotificacionDocument doc = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", notificacionId));

        boolean esMia = cuentaId.equals(doc.getCompradorId())
                || cuentaId.equals(doc.getCocineraId());
        if (!esMia) {
            throw new AccesoDenegadoException("No puedes modificar esta notificación");
        }

        doc.setLeida(true);
        notificacionRepository.save(doc);
    }

    @Override
    public long contarNoLeidas(Long cuentaId) {
        return notificacionRepository
                .countByLeidaFalseAndCompradorIdOrLeidaFalseAndCocineraId(cuentaId, cuentaId);
    }

    @Override
    public void registrar(Notificacion notificacion) {
        if (notificacion == null) return;
        notificacionRepository.save(notificacionMapper.toDocument(notificacion));
    }

    private NotificacionResponseDTO toResponse(NotificacionDocument doc) {
        return NotificacionResponseDTO.builder()
                .id(doc.getId())
                .titulo(doc.getTitulo())
                .mensaje(doc.getMensaje())
                .tipo(doc.getTipo())
                .rolDestinatario(doc.getRolDestinatario())
                .leida(doc.isLeida())
                .fechaCreacion(doc.getFechaCreacion())
                .build();
    }
}