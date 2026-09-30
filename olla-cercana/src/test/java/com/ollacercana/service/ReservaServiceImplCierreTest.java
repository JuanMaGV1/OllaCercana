package com.ollacercana.service;

import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.exception.ReservaModificadaException;
import com.ollacercana.exception.ReservaNoConfirmadaException;
import com.ollacercana.exception.ReservaNoEncontradaException;
import com.ollacercana.model.domain.*;
import com.ollacercana.observer.ObservadorReserva;
import com.ollacercana.observer.PublicadorEventosReserva;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReporteRepository;
import com.ollacercana.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OC-161: pruebas unitarias del cierre de la transacción — HU-23.
 * Cubre OC-156 (completar), OC-157 (cierre automático), OC-158 (reporte abierto) y OC-159 (chat en solo lectura).
 */
@ExtendWith(MockitoExtension.class)
class ReservaServiceImplCierreTest {

    private static final UUID COCINERA_ID = UUID.randomUUID();
    private static final Long COMPRADOR_ID = 42L;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private PlatoRepository platoRepository;

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private ObservadorReserva observador;

    private com.ollacercana.service.impl.ReservaServiceImpl reservaService;

    @BeforeEach
    void setUp() {
        PublicadorEventosReserva publicador = new PublicadorEventosReserva(List.of(observador));
        reservaService = new com.ollacercana.service.impl.ReservaServiceImpl(reservaRepository, platoRepository, reporteRepository, publicador);

        lenient().when(reservaRepository.saveAndFlush(any(com.ollacercana.model.domain.Reserva.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ============ Datos de prueba ============

    private Plato plato() {
        return Plato.builder()
                .id(UUID.randomUUID())
                .cocineraId(COCINERA_ID)
                .nombre("Ajiaco santafereño")
                .porcionesTotales(5)
                .porcionesComprometidas(2)
                .precioPorcion(new BigDecimal("16000"))
                .estado(EstadoPlato.ACTIVO)
                .version(0)
                .build();
    }

    /** Reserva CONFIRMADA hace las horas indicadas (con chat ACTIVO), registrada en el repositorio simulado. */
    private com.ollacercana.model.domain.Reserva reservaConfirmada(int horasDesdeConfirmacion) {
        LocalDateTime confirmadaEn = LocalDateTime.now().minusHours(horasDesdeConfirmacion);
        com.ollacercana.model.domain.Reserva reserva = com.ollacercana.model.domain.Reserva.crear(plato(), COMPRADOR_ID, 2, MedioPago.EFECTIVO, null, confirmadaEn.minusMinutes(2));
        reserva.confirmar(confirmadaEn.plusHours(1), confirmadaEn);
        lenient().when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
        return reserva;
    }

    private com.ollacercana.model.domain.Reserva reservaEnEstado(EstadoReserva estado) {
        com.ollacercana.model.domain.Reserva reserva = com.ollacercana.model.domain.Reserva.crear(plato(), COMPRADOR_ID, 2, MedioPago.EFECTIVO, null, LocalDateTime.now().minusMinutes(1));
        reserva.setEstado(estado);
        lenient().when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
        return reserva;
    }

    private EventoReserva eventoPublicado() {
        ArgumentCaptor<EventoReserva> captor = ArgumentCaptor.forClass(EventoReserva.class);
        verify(observador).notificar(captor.capture());
        return captor.getValue();
    }

    // ============ OC-156 / OC-159: happy path ============

    @Test
    @DisplayName("Escenario 1: completar pasa a COMPLETADA, chat en SOLO_LECTURA y habilita la calificación")
    void completar_debeCompletarPonerChatEnSoloLecturaYHabilitarCalificacion() {
        com.ollacercana.model.domain.Reserva reserva = reservaConfirmada(1);
        assertEquals(com.ollacercana.model.domain.EstadoChat.ACTIVO, reserva.getEstadoChat());
        when(reporteRepository.existsByReservaIdAndEstado(reserva.getId(), com.ollacercana.model.domain.EstadoReporte.ABIERTO)).thenReturn(false);

        com.ollacercana.model.domain.Reserva completada = reservaService.completar(reserva.getId(), "Todo bien");

        assertEquals(EstadoReserva.COMPLETADA, completada.getEstado());
        assertEquals(com.ollacercana.model.domain.EstadoChat.SOLO_LECTURA, completada.getEstadoChat());
        assertTrue(completada.isCalificacionHabilitada());
        assertEquals("Todo bien", completada.getComentarioCierre());
        assertNotNull(completada.getFechaCompletada());
        verify(reservaRepository).saveAndFlush(reserva);
        // No se devuelven porciones: la comida sí se entregó
        verify(platoRepository, never()).save(any());

        EventoReserva evento = eventoPublicado();
        assertEquals(com.ollacercana.model.domain.TipoEvento.RESERVA_COMPLETADA, evento.tipo());
        assertEquals(COMPRADOR_ID, evento.compradorId());
        assertEquals(false, evento.payload().get("automatica"));
    }

    @Test
    void completar_sinComentario_debeGuardarComentarioNulo() {
        com.ollacercana.model.domain.Reserva reserva = reservaConfirmada(1);

        com.ollacercana.model.domain.Reserva completada = reservaService.completar(reserva.getId(), "   ");

        assertEquals(EstadoReserva.COMPLETADA, completada.getEstado());
        assertNull(completada.getComentarioCierre());
    }

    // ============ 404 ============

    @Test
    void completar_reservaNoExiste_debeLanzarNoEncontrada() {
        UUID inexistente = UUID.randomUUID();
        when(reservaRepository.findById(inexistente)).thenReturn(Optional.empty());

        assertThrows(ReservaNoEncontradaException.class, () -> reservaService.completar(inexistente, null));
        verify(reservaRepository, never()).saveAndFlush(any());
        verifyNoInteractions(observador);
    }

    // ============ Escenario 4: reserva no confirmada -> 422 ============

    @Test
    @DisplayName("Escenario 4: PENDIENTE, RECHAZADA o EXPIRADA no se pueden cerrar")
    void completar_reservaNoConfirmada_debeLanzarReglaDeNegocioSinGuardar() {
        for (EstadoReserva estado : List.of(EstadoReserva.PENDIENTE, EstadoReserva.RECHAZADA, EstadoReserva.EXPIRADA)) {
            com.ollacercana.model.domain.Reserva reserva = reservaEnEstado(estado);
            UUID id = reserva.getId();

            assertThrows(ReservaNoConfirmadaException.class, () -> reservaService.completar(id, null),
                    "Debe rechazar el cierre en estado " + estado);
            assertEquals(estado, reserva.getEstado());
        }
        verify(reservaRepository, never()).saveAndFlush(any());
        verifyNoInteractions(observador, reporteRepository);
    }

    @Test
    void completar_reservaYaCompletada_debeLanzarReglaDeNegocio() {
        com.ollacercana.model.domain.Reserva reserva = reservaEnEstado(EstadoReserva.COMPLETADA);
        UUID id = reserva.getId();

        assertThrows(ReglaDeNegocioException.class, () -> reservaService.completar(id, null));
        verify(reservaRepository, never()).saveAndFlush(any());
    }

    // ============ OC-158 / Escenario 3: reporte abierto -> 422 ============

    @Test
    @DisplayName("Escenario 3: con un reporte ABIERTO el cierre se bloquea (422) y la reserva no cambia")
    void completar_conReporteAbierto_debeLanzarReglaDeNegocioSinCambios() {
        com.ollacercana.model.domain.Reserva reserva = reservaConfirmada(1);
        UUID id = reserva.getId();
        when(reporteRepository.existsByReservaIdAndEstado(id, com.ollacercana.model.domain.EstadoReporte.ABIERTO)).thenReturn(true);

        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class, () -> reservaService.completar(id, null));

        assertTrue(ex.getMessage().contains("reporte abierto"));
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(com.ollacercana.model.domain.EstadoChat.ACTIVO, reserva.getEstadoChat());
        assertFalse(reserva.isCalificacionHabilitada());
        verify(reservaRepository, never()).saveAndFlush(any());
        verifyNoInteractions(observador);
    }

    @Test
    void completar_conflictoDeVersion_debeLanzarReservaModificada() {
        com.ollacercana.model.domain.Reserva reserva = reservaConfirmada(1);
        UUID id = reserva.getId();
        when(reservaRepository.saveAndFlush(any(com.ollacercana.model.domain.Reserva.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(com.ollacercana.model.domain.Reserva.class, id));

        assertThrows(ReservaModificadaException.class, () -> reservaService.completar(id, null));
        verifyNoInteractions(observador);
    }

    // ============ OC-157 / Escenario 2: cierre automático a las 24 h ============

    @Test
    void buscarReservasParaCierreAutomatico_debeConsultarConfirmadasConLimiteDe24Horas() {
        com.ollacercana.model.domain.Reserva vieja = reservaConfirmada(30);
        when(reservaRepository.findByEstadoAndFechaDecisionLessThanEqual(eq(EstadoReserva.CONFIRMADA), any(LocalDateTime.class)))
                .thenReturn(List.of(vieja));
        LocalDateTime antes = LocalDateTime.now().minusHours(24);

        List<UUID> ids = reservaService.buscarReservasParaCierreAutomatico();

        LocalDateTime despues = LocalDateTime.now().minusHours(24);
        assertEquals(List.of(vieja.getId()), ids);
        ArgumentCaptor<LocalDateTime> limite = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(reservaRepository).findByEstadoAndFechaDecisionLessThanEqual(eq(EstadoReserva.CONFIRMADA), limite.capture());
        assertFalse(limite.getValue().isBefore(antes), "El límite debe ser 'ahora - 24 h'");
        assertFalse(limite.getValue().isAfter(despues), "El límite debe ser 'ahora - 24 h'");
    }

    @Test
    @DisplayName("Escenario 2: a las 24 h sin cierre la reserva se completa sola y se notifica a ambas partes")
    void completarAutomaticamente_a24Horas_debeCompletarYPublicarEventoAutomatico() {
        com.ollacercana.model.domain.Reserva reserva = reservaConfirmada(25);
        when(reporteRepository.existsByReservaIdAndEstado(reserva.getId(), com.ollacercana.model.domain.EstadoReporte.ABIERTO)).thenReturn(false);

        com.ollacercana.model.domain.Reserva completada = reservaService.completarAutomaticamente(reserva.getId());

        assertEquals(EstadoReserva.COMPLETADA, completada.getEstado());
        assertEquals(com.ollacercana.model.domain.EstadoChat.SOLO_LECTURA, completada.getEstadoChat());
        assertTrue(completada.isCalificacionHabilitada());
        verify(reservaRepository).saveAndFlush(reserva);

        EventoReserva evento = eventoPublicado();
        assertEquals(com.ollacercana.model.domain.TipoEvento.RESERVA_COMPLETADA, evento.tipo());
        assertEquals(true, evento.payload().get("automatica"));
    }

    @Test
    void completarAutomaticamente_antesDe24Horas_noDebeHacerNada() {
        com.ollacercana.model.domain.Reserva reserva = reservaConfirmada(23);

        com.ollacercana.model.domain.Reserva resultado = reservaService.completarAutomaticamente(reserva.getId());

        assertEquals(EstadoReserva.CONFIRMADA, resultado.getEstado());
        verify(reservaRepository, never()).saveAndFlush(any());
        verifyNoInteractions(observador, reporteRepository);
    }

    @Test
    void completarAutomaticamente_siYaSeCerro_noDebeHacerNada() {
        com.ollacercana.model.domain.Reserva reserva = reservaEnEstado(EstadoReserva.COMPLETADA);

        com.ollacercana.model.domain.Reserva resultado = reservaService.completarAutomaticamente(reserva.getId());

        assertEquals(EstadoReserva.COMPLETADA, resultado.getEstado());
        verify(reservaRepository, never()).saveAndFlush(any());
        verifyNoInteractions(observador);
    }

    @Test
    void completarAutomaticamente_conReporteAbierto_noDebeCompletar() {
        com.ollacercana.model.domain.Reserva reserva = reservaConfirmada(48);
        when(reporteRepository.existsByReservaIdAndEstado(reserva.getId(), com.ollacercana.model.domain.EstadoReporte.ABIERTO)).thenReturn(true);

        com.ollacercana.model.domain.Reserva resultado = reservaService.completarAutomaticamente(reserva.getId());

        assertEquals(EstadoReserva.CONFIRMADA, resultado.getEstado());
        assertEquals(com.ollacercana.model.domain.EstadoChat.ACTIVO, resultado.getEstadoChat());
        verify(reservaRepository, never()).saveAndFlush(any());
        verifyNoInteractions(observador);
    }
}
