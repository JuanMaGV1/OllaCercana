package com.ollacercana.core.services;

import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.dtos.request.EnviarMensajeRequestDTO;
import com.ollacercana.controller.dtos.response.MensajeResponseDTO;
import com.ollacercana.controller.handlers.exception.AccesoDenegadoException;
import com.ollacercana.controller.handlers.exception.ConflictoException;
import com.ollacercana.controller.handlers.exception.ReservaNoEncontradaException;
import com.ollacercana.controller.mappers.ChatMapper;
import com.ollacercana.core.models.EventoReserva;
import com.ollacercana.core.models.enums.EstadoChat;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.models.enums.TipoEvento;
import com.ollacercana.core.patterns.observer.PublicadorEventosReserva;
import com.ollacercana.core.services.impl.ChatServiceImpl;
import com.ollacercana.core.validators.ChatValidator;
import com.ollacercana.persistence.document.MensajeChatDocument;
import com.ollacercana.persistence.entities.ReservaEntity;
import com.ollacercana.persistence.repository.ReservaRepository;
import com.ollacercana.persistence.repository.mongo.MensajeChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    private ReservaRepository reservaRepository;

    @Mock
    private ChatValidator chatValidator;

    @Mock
    private PublicadorEventosReserva publicadorEventosReserva;

    @Spy
    private ChatMapper chatMapper = new ChatMapper();

    @Mock
    private UsuarioActual usuarioActual;

    @InjectMocks
    private ChatServiceImpl chatService;

    private final UUID reservaId = UUID.randomUUID();
    private final UUID cocineraId = UUID.randomUUID();
    private final Long compradorId = 15L;

    @BeforeEach
    void setUp() {
        chatService.setPublicadorEventosReserva(publicadorEventosReserva);
    }

    // =========================================================================
    // OC-263: Enviar mensaje, guardar y publicar evento NUEVO_MENSAJE_CHAT
    // =========================================================================

    @Test
    @DisplayName("OC-263: enviar guarda el mensaje y publica el evento NUEVO_MENSAJE_CHAT")
    void enviar_GuardaYPublicaEvento() {
        // Arrange
        EnviarMensajeRequestDTO request = new EnviarMensajeRequestDTO("Hola, ¿mi pedido ya está listo?");
        ReservaEntity reserva = ReservaEntity.builder()
                .id(reservaId)
                .platoId(UUID.randomUUID())
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
        MensajeResponseDTO respuesta = chatService.enviar(reservaId, request);

        // Assert
        assertNotNull(respuesta);
        assertEquals("mongo-id-123", respuesta.getId());
        assertEquals("Hola, ¿mi pedido ya está listo?", respuesta.getTexto());
        assertEquals(Rol.COMPRADOR, respuesta.getAutorRol());
        assertEquals(compradorId.toString(), respuesta.getAutorId());
        assertFalse(respuesta.isLeido());

        verify(mensajeChatRepository, times(1)).save(any(MensajeChatDocument.class));

        ArgumentCaptor<EventoReserva> eventoCaptor = ArgumentCaptor.forClass(EventoReserva.class);
        verify(publicadorEventosReserva, times(1)).publicar(eventoCaptor.capture());
        EventoReserva eventoPublicado = eventoCaptor.getValue();
        assertEquals(TipoEvento.NUEVO_MENSAJE_CHAT, eventoPublicado.tipo());
        assertEquals(reservaId, eventoPublicado.reservaId());
        assertEquals("Hola, ¿mi pedido ya está listo?", eventoPublicado.payload().get("texto"));
    }

    @Test
    @DisplayName("OC-263: Enviar mensaje a reserva inexistente lanza 404")
    void enviar_ReservaInexistente_Lanza404() {
        // Arrange
        EnviarMensajeRequestDTO request = new EnviarMensajeRequestDTO("Hola");
        when(usuarioActual.getCuentaId()).thenReturn(compradorId);
        when(usuarioActual.getCocineraIdOpt()).thenReturn(Optional.empty());
        when(chatValidator.validarParaEnviarMensaje(reservaId, compradorId, null, "Hola"))
                .thenThrow(new ReservaNoEncontradaException(reservaId));

        // Act & Assert
        assertThrows(ReservaNoEncontradaException.class, () -> chatService.enviar(reservaId, request));
    }

    @Test
    @DisplayName("OC-263: Enviar mensaje como usuario ajeno lanza 403")
    void enviar_UsuarioAjeno_Lanza403() {
        // Arrange
        EnviarMensajeRequestDTO request = new EnviarMensajeRequestDTO("Hola");
        when(usuarioActual.getCuentaId()).thenReturn(99L);
        when(usuarioActual.getCocineraIdOpt()).thenReturn(Optional.empty());
        when(chatValidator.validarParaEnviarMensaje(reservaId, 99L, null, "Hola"))
                .thenThrow(new AccesoDenegadoException("No tienes permiso sobre este chat"));

        // Act & Assert
        assertThrows(AccesoDenegadoException.class, () -> chatService.enviar(reservaId, request));
    }

    @Test
    @DisplayName("OC-263: Enviar mensaje en chat finalizado lanza 409 con 'Chat finalizado'")
    void enviar_ChatFinalizado_Lanza409() {
        // Arrange
        EnviarMensajeRequestDTO request = new EnviarMensajeRequestDTO("Hola");
        when(usuarioActual.getCuentaId()).thenReturn(compradorId);
        when(usuarioActual.getCocineraIdOpt()).thenReturn(Optional.empty());
        when(chatValidator.validarParaEnviarMensaje(reservaId, compradorId, null, "Hola"))
                .thenThrow(new ConflictoException("Chat finalizado"));

        // Act & Assert
        ConflictoException ex = assertThrows(ConflictoException.class, () -> chatService.enviar(reservaId, request));
        assertEquals("Chat finalizado", ex.getMessage());
    }

    // =========================================================================
    // OC-264: Listar para polling con cursor 'desde' y soporte de SOLO_LECTURA
    // =========================================================================

    @Test
    @DisplayName("OC-264: Polling con cursor retorna sólo mensajes posteriores a la fecha dada")
    void listar_ConCursor_RetornaMensajesPosteriores() {
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
        List<MensajeResponseDTO> resultado = chatService.listar(reservaId, cursor);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Mensaje nuevo", resultado.get(0).getTexto());
    }

    @Test
    @DisplayName("OC-264: Polling con cursor retorna lista vacía si no hay mensajes nuevos")
    void listar_ConCursor_RetornaListaVaciaSiNoHay() {
        // Arrange
        LocalDateTime cursor = LocalDateTime.now().minusMinutes(1);
        when(usuarioActual.getCuentaId()).thenReturn(compradorId);
        when(usuarioActual.getCocineraIdOpt()).thenReturn(Optional.empty());
        when(mensajeChatRepository.findByReservaIdAndFechaAfterOrderByFechaAsc(reservaId, cursor))
                .thenReturn(List.of());

        // Act
        List<MensajeResponseDTO> resultado = chatService.listar(reservaId, cursor);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("OC-264: Listar funciona correctamente aunque el chat esté en SOLO_LECTURA")
    void listar_ChatSoloLectura_PermiteConsultar() {
        // Arrange
        ReservaEntity reservaCerrada = ReservaEntity.builder()
                .id(reservaId)
                .compradorId(compradorId)
                .cocineraId(cocineraId)
                .estadoChat(EstadoChat.SOLO_LECTURA)
                .build();

        MensajeChatDocument historico = MensajeChatDocument.builder()
                .id("doc-hist")
                .reservaId(reservaId)
                .texto("Mensaje histórico de la entrega")
                .fecha(LocalDateTime.now().minusHours(2))
                .autorRol(Rol.COCINERA)
                .autorId(cocineraId.toString())
                .build();

        when(usuarioActual.getCuentaId()).thenReturn(compradorId);
        when(usuarioActual.getCocineraIdOpt()).thenReturn(Optional.empty());
        when(chatValidator.validarAccesoChat(reservaId, compradorId, null)).thenReturn(reservaCerrada);
        when(mensajeChatRepository.findByReservaIdOrderByFechaAsc(reservaId)).thenReturn(List.of(historico));

        // Act
        List<MensajeResponseDTO> resultado = chatService.listar(reservaId, null);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Mensaje histórico de la entrega", resultado.get(0).getTexto());
    }

    // =========================================================================
    // OC-265: Mensajes no leídos y conteo baja a cero al marcarlos leídos
    // =========================================================================

    @Test
    @DisplayName("OC-265: Conteo de no leídos baja a cero al marcar leídos los mensajes de la contraparte")
    void mensajesNoLeidos_ConteoBajaACeroAlMarcarLeidos() {
        // Arrange
        ReservaEntity reserva = ReservaEntity.builder()
                .id(reservaId)
                .compradorId(compradorId)
                .cocineraId(cocineraId)
                .build();

        MensajeChatDocument msg1 = MensajeChatDocument.builder()
                .id("msg-1").reservaId(reservaId).autorRol(Rol.COCINERA).leido(false).build();
        MensajeChatDocument msg2 = MensajeChatDocument.builder()
                .id("msg-2").reservaId(reservaId).autorRol(Rol.COCINERA).leido(false).build();

        when(usuarioActual.getCuentaId()).thenReturn(compradorId);
        when(usuarioActual.getCocineraIdOpt()).thenReturn(Optional.empty());
        when(chatValidator.validarAccesoChat(reservaId, compradorId, null)).thenReturn(reserva);

        // Antes de marcar leídos, count devuelve 2
        when(mensajeChatRepository.countByReservaIdAndLeidoFalseAndAutorRolNot(reservaId, Rol.COMPRADOR))
                .thenReturn(2L)
                .thenReturn(0L); // Después de marcar, count devuelve 0

        when(mensajeChatRepository.findByReservaIdAndLeidoFalse(reservaId)).thenReturn(List.of(msg1, msg2));

        // Act 1: Conteo inicial
        long conteoAntes = chatService.contarNoLeidos(reservaId);
        assertEquals(2L, conteoAntes);

        // Act 2: Marcar como leídos
        chatService.marcarLeidos(reservaId);

        // Assert & Act 3: Conteo después de marcar
        assertTrue(msg1.isLeido());
        assertTrue(msg2.isLeido());
        verify(mensajeChatRepository, times(1)).save(msg1);
        verify(mensajeChatRepository, times(1)).save(msg2);

        long conteoDespues = chatService.contarNoLeidos(reservaId);
        assertEquals(0L, conteoDespues);
    }

    // =========================================================================
    // OC-266: Purga de mensajes de reservas cerradas hace más de 30 días
    // =========================================================================

    @Test
    @DisplayName("OC-266: Reserva cerrada hace 31 días elimina sus mensajes")
    void purgarMensajes_ReservaCerradaHace31Dias_EliminaMensajes() {
        // Arrange
        UUID reservaCerradaViejaId = UUID.randomUUID();
        ReservaEntity reservaVieja = ReservaEntity.builder()
                .id(reservaCerradaViejaId)
                .estado(EstadoReserva.COMPLETADA)
                .fechaCompletada(LocalDateTime.now().minusDays(31))
                .build();

        when(reservaRepository.findByEstadoAndFechaCompletadaLessThanEqual(eq(EstadoReserva.COMPLETADA), any(LocalDateTime.class)))
                .thenReturn(List.of(reservaVieja));
        when(mensajeChatRepository.deleteByReservaId(reservaCerradaViejaId)).thenReturn(4L);

        // Act
        long borrados = chatService.purgarMensajesAntiguos();

        // Assert
        assertEquals(4L, borrados);
        verify(mensajeChatRepository, times(1)).deleteByReservaId(reservaCerradaViejaId);
    }

    @Test
    @DisplayName("OC-266: Sin reservas cerradas hace más de 30 días no elimina ningún mensaje")
    void purgarMensajes_SinReservasAntiguas_NoElimina() {
        // Arrange
        when(reservaRepository.findByEstadoAndFechaCompletadaLessThanEqual(eq(EstadoReserva.COMPLETADA), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // Act
        long borrados = chatService.purgarMensajesAntiguos();

        // Assert
        assertEquals(0L, borrados);
        verify(mensajeChatRepository, never()).deleteByReservaId(any());
    }
}