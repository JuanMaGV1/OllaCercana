package com.ollacercana.validator;

import com.ollacercana.controller.handlers.exception.AccesoDenegadoException;
import com.ollacercana.controller.handlers.exception.ConflictoException;
import com.ollacercana.controller.handlers.exception.ReservaNoEncontradaException;
import com.ollacercana.core.models.enums.EstadoChat;
import com.ollacercana.core.validators.ChatValidator;
import com.ollacercana.persistence.entities.ReservaEntity;
import com.ollacercana.persistence.repository.ReservaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatValidatorTest {

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ChatValidator validator;

    private final UUID reservaId = UUID.randomUUID();
    private final UUID cocineraId = UUID.randomUUID();
    private final Long compradorId = 10L;

    @Test
    @DisplayName("Reserva inexistente lanza 404 ReservaNoEncontradaException")
    void validarParaEnviarMensaje_ReservaNoExiste_Lanza404() {
        // Arrange
        when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ReservaNoEncontradaException.class,
                () -> validator.validarParaEnviarMensaje(reservaId, compradorId, null, "Hola"));
    }

    @Test
    @DisplayName("Usuario ajeno a la reserva lanza 403 AccesoDenegadoException")
    void validarParaEnviarMensaje_UsuarioAjeno_Lanza403() {
        // Arrange
        ReservaEntity reserva = ReservaEntity.builder()
                .id(reservaId)
                .compradorId(compradorId)
                .cocineraId(cocineraId)
                .estadoChat(EstadoChat.ACTIVO)
                .build();
        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));

        // Act & Assert (Usuario con cuentaId 99L ajeno)
        assertThrows(AccesoDenegadoException.class,
                () -> validator.validarParaEnviarMensaje(reservaId, 99L, UUID.randomUUID(), "Hola"));
    }

    @Test
    @DisplayName("Chat no ACTIVO lanza 409 con mensaje 'Chat finalizado'")
    void validarParaEnviarMensaje_ChatFinalizado_Lanza409() {
        // Arrange
        ReservaEntity reserva = ReservaEntity.builder()
                .id(reservaId)
                .compradorId(compradorId)
                .cocineraId(cocineraId)
                .estadoChat(EstadoChat.SOLO_LECTURA)
                .build();
        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));

        // Act & Assert
        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> validator.validarParaEnviarMensaje(reservaId, compradorId, null, "Hola"));
        assertEquals("Chat finalizado", ex.getMessage());
    }

    @Test
    @DisplayName("Texto vacío o con puros espacios lanza IllegalArgumentException")
    void validarTexto_Vacio_LanzaExcepcion() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validator.validarTexto(""));
        assertThrows(IllegalArgumentException.class, () -> validator.validarTexto("   "));
        assertThrows(IllegalArgumentException.class, () -> validator.validarTexto(null));
    }

    @Test
    @DisplayName("Texto que supera 500 caracteres lanza IllegalArgumentException")
    void validarTexto_DemasiadoLargo_LanzaExcepcion() {
        // Arrange
        String textoLargo = "a".repeat(501);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validator.validarTexto(textoLargo));
    }
}