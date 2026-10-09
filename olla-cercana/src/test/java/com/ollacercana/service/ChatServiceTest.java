package com.ollacercana.service;

import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.dtos.request.EnviarMensajeRequestDTO;
import com.ollacercana.controller.dtos.response.MensajeResponseDTO;
import com.ollacercana.controller.mappers.ChatMapper;
import com.ollacercana.core.models.enums.EstadoChat;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.services.impl.ChatServiceImpl;
import com.ollacercana.core.validators.ChatValidator;
import com.ollacercana.persistence.document.MensajeChatDocument;
import com.ollacercana.persistence.entities.ReservaEntity;
import com.ollacercana.persistence.repository.mongo.MensajeChatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private MensajeChatRepository mensajeChatRepository;

    @Mock
    private ChatValidator chatValidator;

    @Spy
    private ChatMapper chatMapper = new ChatMapper();

    @Mock
    private UsuarioActual usuarioActual;

    @InjectMocks
    private ChatServiceImpl chatService;

    private final UUID reservaId = UUID.randomUUID();
    private final UUID cocineraId = UUID.randomUUID();
    private final Long compradorId = 15L;

    @Test
    @DisplayName("Enviar mensaje con éxito: guarda y retorna DTO con rol y texto")
    void enviarMensaje_Exitoso() {
        // Arrange
        EnviarMensajeRequestDTO request = new EnviarMensajeRequestDTO("Hola, ya voy en camino.");
        ReservaEntity reserva = ReservaEntity.builder()
                .id(reservaId)
                .compradorId(compradorId)
                .cocineraId(cocineraId)
                .estadoChat(EstadoChat.ACTIVO)
                .build();

        when(usuarioActual.getCuentaId()).thenReturn(compradorId);
        when(usuarioActual.getCocineraIdOpt()).thenReturn(Optional.empty());
        when(chatValidator.validarParaEnviarMensaje(reservaId, compradorId, null, request.texto())).thenReturn(reserva);

        when(mensajeChatRepository.save(any(MensajeChatDocument.class))).thenAnswer(inv -> {
            MensajeChatDocument doc = inv.getArgument(0);
            doc.setId("mongo-id-123");
            return doc;
        });

        // Act
        MensajeResponseDTO respuesta = chatService.enviarMensaje(reservaId, request);

        // Assert
        assertNotNull(respuesta);
        assertEquals("mongo-id-123", respuesta.getId());
        assertEquals("Hola, ya voy en camino.", respuesta.getTexto());
        assertEquals(Rol.COMPRADOR, respuesta.getAutorRol());
        assertEquals(compradorId.toString(), respuesta.getAutorId());
        assertFalse(respuesta.isLeido());
        verify(mensajeChatRepository, times(1)).save(any(MensajeChatDocument.class));
    }

    @Test
    @DisplayName("Polling con cursor: retorna sólo mensajes posteriores a la fecha dada")
    void listarMensajes_ConCursor_RetornaMensajesPosteriores() {
        // Arrange
        LocalDateTime cursor = LocalDateTime.now().minusMinutes(5);
        MensajeChatDocument mensaje = MensajeChatDocument.builder()
                .id("doc-1")
                .reservaId(reservaId)
                .texto("Mensaje nuevo")
                .fecha(LocalDateTime.now())
                .autorRol(Rol.COCINERA)
                .autorId(cocineraId.toString())
                .build();

        when(usuarioActual.getCuentaId()).thenReturn(compradorId);
        when(usuarioActual.getCocineraIdOpt()).thenReturn(Optional.empty());
        when(mensajeChatRepository.findByReservaIdAndFechaAfterOrderByFechaAsc(reservaId, cursor))
                .thenReturn(List.of(mensaje));

        // Act
        List<MensajeResponseDTO> resultado = chatService.listarMensajes(reservaId, cursor);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Mensaje nuevo", resultado.get(0).getTexto());
    }

    @Test
    @DisplayName("Polling sin mensajes: retorna lista vacía sin fallar")
    void listarMensajes_SinMensajes_RetornaListaVacia() {
        // Arrange
        when(usuarioActual.getCuentaId()).thenReturn(compradorId);
        when(usuarioActual.getCocineraIdOpt()).thenReturn(Optional.empty());
        when(mensajeChatRepository.findByReservaIdOrderByFechaAsc(reservaId)).thenReturn(List.of());

        // Act
        List<MensajeResponseDTO> resultado = chatService.listarMensajes(reservaId, null);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Conteo de no leídos: consulta correctamente para el destinatario")
    void contarNoLeidos_RetornaConteoCorrecto() {
        // Arrange
        ReservaEntity reserva = ReservaEntity.builder()
                .id(reservaId)
                .compradorId(compradorId)
                .cocineraId(cocineraId)
                .build();

        when(usuarioActual.getCuentaId()).thenReturn(compradorId);
        when(usuarioActual.getCocineraIdOpt()).thenReturn(Optional.empty());
        when(chatValidator.validarAccesoChat(reservaId, compradorId, null)).thenReturn(reserva);
        when(mensajeChatRepository.countByReservaIdAndLeidoFalseAndAutorRolNot(reservaId, Rol.COMPRADOR))
                .thenReturn(3L);

        // Act
        long conteo = chatService.contarNoLeidos(reservaId);

        // Assert
        assertEquals(3L, conteo);
    }

    @Test
    @DisplayName("Marcar leídos: actualiza únicamente los mensajes de la contraparte")
    void marcarLeidos_ActualizaMensajesDeLaOtraParte() {
        // Arrange
        ReservaEntity reserva = ReservaEntity.builder()
                .id(reservaId)
                .compradorId(compradorId)
                .cocineraId(cocineraId)
                .build();

        MensajeChatDocument msgCocinera = MensajeChatDocument.builder()
                .id("m-cocinera")
                .autorRol(Rol.COCINERA)
                .leido(false)
                .build();

        when(usuarioActual.getCuentaId()).thenReturn(compradorId);
        when(usuarioActual.getCocineraIdOpt()).thenReturn(Optional.empty());
        when(chatValidator.validarAccesoChat(reservaId, compradorId, null)).thenReturn(reserva);
        when(mensajeChatRepository.findByReservaIdAndLeidoFalse(reservaId)).thenReturn(List.of(msgCocinera));

        // Act
        chatService.marcarLeidos(reservaId);

        // Assert
        assertTrue(msgCocinera.isLeido());
        verify(mensajeChatRepository, times(1)).save(msgCocinera);
    }
}
