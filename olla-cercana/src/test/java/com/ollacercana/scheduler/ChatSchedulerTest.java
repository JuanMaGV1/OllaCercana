package com.ollacercana.scheduler;

import com.ollacercana.config.scheduler.ChatScheduler;
import com.ollacercana.core.services.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatSchedulerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatScheduler scheduler;

    @Test
    @DisplayName("OC-266: purgarMensajesAntiguos ejecuta la purga cuando tareas-programadas está habilitado")
    void purgarMensajesAntiguos_flagHabilitado_ejecutaPurga() {
        // Arrange
        ReflectionTestUtils.setField(scheduler, "tareasProgramadasHabilitadas", true);
        when(chatService.purgarMensajesAntiguos()).thenReturn(8L);

        // Act
        scheduler.purgarMensajesAntiguos();

        // Assert
        verify(chatService, times(1)).purgarMensajesAntiguos();
    }

    @Test
    @DisplayName("OC-266: purgarMensajesAntiguos no ejecuta nada cuando tareas-programadas está deshabilitado")
    void purgarMensajesAntiguos_flagDeshabilitado_noEjecuta() {
        // Arrange
        ReflectionTestUtils.setField(scheduler, "tareasProgramadasHabilitadas", false);

        // Act
        scheduler.purgarMensajesAntiguos();

        // Assert
        verify(chatService, never()).purgarMensajesAntiguos();
    }
}