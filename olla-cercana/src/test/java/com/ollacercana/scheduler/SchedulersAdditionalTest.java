package com.ollacercana.scheduler;

import com.ollacercana.config.scheduler.CalificacionScheduler;
import com.ollacercana.config.scheduler.MedallaScheduler;
import com.ollacercana.core.services.CalificacionService;
import com.ollacercana.core.services.MedallaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulersAdditionalTest {

    @Mock CalificacionService calificacionService;
    @Mock MedallaService medallaService;

    @InjectMocks CalificacionScheduler calificacionScheduler;
    @InjectMocks MedallaScheduler medallaScheduler;

    @Test
    @DisplayName("CalificacionScheduler: ejecuta cuando flag está activo")
    void calificacionScheduler_activo() {
        ReflectionTestUtils.setField(calificacionScheduler, "tareasProgramadas", true);
        when(calificacionService.publicarPendientesVencidas(any(LocalDateTime.class))).thenReturn(3);
        calificacionScheduler.publicarPendientes();
        verify(calificacionService).publicarPendientesVencidas(any());
    }

    @Test
    @DisplayName("CalificacionScheduler: no ejecuta cuando flag inactivo")
    void calificacionScheduler_inactivo() {
        ReflectionTestUtils.setField(calificacionScheduler, "tareasProgramadas", false);
        calificacionScheduler.publicarPendientes();
        verifyNoInteractions(calificacionService);
    }

    @Test
    @DisplayName("CalificacionScheduler: no propaga excepción")
    void calificacionScheduler_noPropaga() {
        ReflectionTestUtils.setField(calificacionScheduler, "tareasProgramadas", true);
        when(calificacionService.publicarPendientesVencidas(any())).thenThrow(new RuntimeException("boom"));
        assertDoesNotThrow(() -> calificacionScheduler.publicarPendientes());
    }

    @Test
    @DisplayName("MedallaScheduler: no propaga excepción")
    void medallaScheduler_noPropaga() {
        when(medallaService.calcularBalanceSemanal(any())).thenThrow(new RuntimeException("boom"));
        assertDoesNotThrow(() -> medallaScheduler.calcularBalanceSemanal());
    }
}