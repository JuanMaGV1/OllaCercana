package com.ollacercana.core.services;

import com.ollacercana.controller.handlers.exception.AccesoDenegadoException;
import com.ollacercana.controller.handlers.exception.ResourceNotFoundException;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.models.enums.TipoNotificacion;
import com.ollacercana.core.services.impl.NotificacionServiceImpl;
import com.ollacercana.persistence.document.NotificacionDocument;
import com.ollacercana.persistence.mappers.NotificacionDocumentMapper;
import com.ollacercana.persistence.repository.mongo.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceImplTest {

    @Mock private NotificacionRepository notificacionRepository;
    @Mock private NotificacionDocumentMapper notificacionMapper;

    private NotificacionServiceImpl service;

    private static final Long CUENTA_ID = 42L;

    @BeforeEach
    void setUp() {
        service = new NotificacionServiceImpl(notificacionRepository, notificacionMapper);
    }

    @Test
    @DisplayName("HU-17: listar devuelve solo las notificaciones del usuario")
    void listar_retornaSoloDelUsuario() {
        NotificacionDocument doc = NotificacionDocument.builder()
                .id("n1").compradorId(CUENTA_ID).rolDestinatario(Rol.COMPRADOR)
                .titulo("X").mensaje("Y").tipo(TipoNotificacion.RESERVA_CONFIRMADA)
                .fechaCreacion(LocalDateTime.now()).build();

        when(notificacionRepository.findByCompradorIdOrCocineraIdOrderByFechaCreacionDesc(
                CUENTA_ID, CUENTA_ID)).thenReturn(List.of(doc));

        var result = service.listarParaUsuario(CUENTA_ID);

        assertEquals(1, result.size());
        assertEquals("n1", result.get(0).getId());
    }

    @Test
    @DisplayName("HU-17: marcarLeida rechaza notificaciones de otro usuario")
    void marcarLeida_ajena_lanza403() {
        NotificacionDocument doc = NotificacionDocument.builder()
                .id("n1").compradorId(99L).build();
        when(notificacionRepository.findById("n1")).thenReturn(Optional.of(doc));

        assertThrows(AccesoDenegadoException.class,
                () -> service.marcarLeida("n1", CUENTA_ID));
        verify(notificacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("HU-17: marcarLeida inexistente lanza 404")
    void marcarLeida_noExiste_lanza404() {
        when(notificacionRepository.findById("n1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.marcarLeida("n1", CUENTA_ID));
    }

    @Test
    @DisplayName("HU-17: marcarLeida cambia el estado")
    void marcarLeida_exitoso() {
        NotificacionDocument doc = NotificacionDocument.builder()
                .id("n1").compradorId(CUENTA_ID).leida(false).build();
        when(notificacionRepository.findById("n1")).thenReturn(Optional.of(doc));
        when(notificacionRepository.save(doc)).thenReturn(doc);

        service.marcarLeida("n1", CUENTA_ID);

        assertTrue(doc.isLeida());
        verify(notificacionRepository).save(doc);
    }

    @Test
    @DisplayName("HU-17: contarNoLeidas devuelve el número correcto")
    void contarNoLeidas() {
        when(notificacionRepository.countByLeidaFalseAndCompradorIdOrLeidaFalseAndCocineraId(
                CUENTA_ID, CUENTA_ID)).thenReturn(3L);

        assertEquals(3L, service.contarNoLeidas(CUENTA_ID));
    }
}