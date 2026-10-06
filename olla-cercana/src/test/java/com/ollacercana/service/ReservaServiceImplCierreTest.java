package com.ollacercana.service;

import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.exception.ReservaNoConfirmadaException;
import com.ollacercana.exception.ReservaNoEncontradaException;
import com.ollacercana.mapper.EventoMapper;
import com.ollacercana.mapper.PlatoEntityMapper;
import com.ollacercana.mapper.ReservaEntityMapper;
import com.ollacercana.model.domain.*;
import com.ollacercana.observer.ObservadorReserva;
import com.ollacercana.observer.PublicadorEventosReserva;
import com.ollacercana.persistence.entity.ReservaEntity;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReporteRepository;
import com.ollacercana.repository.ReservaRepository;
import com.ollacercana.repository.mongo.EventoReservaRepository;
import com.ollacercana.service.impl.ReservaServiceImpl;
import com.ollacercana.validator.ReservaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplCierreTest {

    private static final UUID COCINERA_ID = UUID.randomUUID();
    private static final Long COMPRADOR_ID = 42L;

    @Mock private ReservaRepository reservaRepository;
    @Mock private PlatoRepository platoRepository;
    @Mock private PerfilCocineraRepository perfilCocineraRepository;
    @Mock private ReporteRepository reporteRepository;
    @Mock private EventoReservaRepository eventoReservaRepository;
    @Mock private ReservaValidator validator;
    @Mock private ReservaEntityMapper reservaEntityMapper;
    @Mock private PlatoEntityMapper platoEntityMapper;
    @Mock private EventoMapper eventoMapper;
    @Mock private ObservadorReserva observador;

    private ReservaServiceImpl reservaService;

    @BeforeEach
    void setUp() {
        PublicadorEventosReserva publicador = new PublicadorEventosReserva(List.of(observador));
        reservaService = new ReservaServiceImpl(
                reservaRepository, platoRepository, perfilCocineraRepository,
                reporteRepository, eventoReservaRepository, publicador,
                validator, reservaEntityMapper, platoEntityMapper, eventoMapper);

        // Mapper genérico: preserva TODOS los campos relevantes.
        lenient().when(reservaEntityMapper.toEntity(any(Reserva.class))).thenAnswer(i -> {
            Reserva r = i.getArgument(0);
            return ReservaEntity.builder()
                    .id(r.getId())
                    .platoId(r.getPlatoId())
                    .cocineraId(r.getCocineraId())
                    .compradorId(r.getCompradorId())
                    .cantidadPorciones(r.getCantidadPorciones())
                    .montoTotal(r.getMontoTotal())
                    .medioPago(r.getMedioPago())
                    .estado(r.getEstado())
                    .notaComprador(r.getNotaComprador())
                    .fechaCreacion(r.getFechaCreacion())
                    .fechaLimiteConfirmacion(r.getFechaLimiteConfirmacion())
                    .fechaDecision(r.getFechaDecision())
                    .horaEstimadaEntrega(r.getHoraEstimadaEntrega())
                    .motivoRechazo(r.getMotivoRechazo())
                    .comentarioRechazo(r.getComentarioRechazo())
                    .recordatorioEnviado(r.isRecordatorioEnviado())
                    .chatHabilitado(r.isChatHabilitado())
                    .estadoChat(r.getEstadoChat())
                    .fechaCompletada(r.getFechaCompletada())
                    .comentarioCierre(r.getComentarioCierre())
                    .calificacionHabilitada(r.isCalificacionHabilitada())
                    .build();
        });

        lenient().when(reservaEntityMapper.toDomain(any(ReservaEntity.class))).thenAnswer(i -> {
            ReservaEntity e = i.getArgument(0);
            return Reserva.builder()
                    .id(e.getId())
                    .platoId(e.getPlatoId())
                    .cocineraId(e.getCocineraId())
                    .compradorId(e.getCompradorId())
                    .cantidadPorciones(e.getCantidadPorciones())
                    .montoTotal(e.getMontoTotal())
                    .medioPago(e.getMedioPago())
                    .estado(e.getEstado())
                    .notaComprador(e.getNotaComprador())
                    .fechaCreacion(e.getFechaCreacion())
                    .fechaLimiteConfirmacion(e.getFechaLimiteConfirmacion())
                    .fechaDecision(e.getFechaDecision())
                    .horaEstimadaEntrega(e.getHoraEstimadaEntrega())
                    .motivoRechazo(e.getMotivoRechazo())
                    .comentarioRechazo(e.getComentarioRechazo())
                    .recordatorioEnviado(e.isRecordatorioEnviado())
                    .chatHabilitado(e.isChatHabilitado())
                    .estadoChat(e.getEstadoChat())
                    .fechaCompletada(e.getFechaCompletada())
                    .comentarioCierre(e.getComentarioCierre())
                    .calificacionHabilitada(e.isCalificacionHabilitada())
                    .build();
        });

        lenient().when(reservaRepository.saveAndFlush(any(ReservaEntity.class)))
                .thenAnswer(i -> i.getArgument(0));
        lenient().when(reservaRepository.save(any(ReservaEntity.class)))
                .thenAnswer(i -> i.getArgument(0));
    }

    private Reserva reservaConfirmada(int horasDesdeConfirmacion) {
        LocalDateTime confirmadaEn = LocalDateTime.now().minusHours(horasDesdeConfirmacion);
        UUID id = UUID.randomUUID();
        UUID platoId = UUID.randomUUID();

        ReservaEntity entidad = ReservaEntity.builder()
                .id(id)
                .platoId(platoId)
                .cocineraId(COCINERA_ID)
                .compradorId(COMPRADOR_ID)
                .cantidadPorciones(2)
                .montoTotal(new BigDecimal("32000"))
                .estado(EstadoReserva.CONFIRMADA)
                .estadoChat(EstadoChat.ACTIVO)
                .chatHabilitado(true)
                .fechaCreacion(confirmadaEn.minusMinutes(2))
                .fechaLimiteConfirmacion(confirmadaEn.plusMinutes(8))
                .fechaDecision(confirmadaEn)
                .build();

        Reserva dominio = Reserva.builder()
                .id(id).platoId(platoId)
                .cocineraId(COCINERA_ID).compradorId(COMPRADOR_ID)
                .cantidadPorciones(2).montoTotal(new BigDecimal("32000"))
                .estado(EstadoReserva.CONFIRMADA)
                .estadoChat(EstadoChat.ACTIVO)
                .chatHabilitado(true)
                .fechaCreacion(entidad.getFechaCreacion())
                .fechaLimiteConfirmacion(entidad.getFechaLimiteConfirmacion())
                .fechaDecision(entidad.getFechaDecision())
                .build();

        when(reservaRepository.findById(id)).thenReturn(Optional.of(entidad));
        when(reservaEntityMapper.toDomain(any(ReservaEntity.class))).thenReturn(dominio);
        return dominio;
    }

    private EventoReserva eventoPublicado() {
        ArgumentCaptor<EventoReserva> captor = ArgumentCaptor.forClass(EventoReserva.class);
        verify(observador).notificar(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("Escenario 1: completar pasa a COMPLETADA, chat en SOLO_LECTURA y habilita la calificación")
    void completar_debeCompletarPonerChatEnSoloLecturaYHabilitarCalificacion() {
        Reserva reserva = reservaConfirmada(1);
        when(reporteRepository.existsByReservaIdAndEstado(reserva.getId(), EstadoReporte.ABIERTO)).thenReturn(false);

        Reserva completada = reservaService.completar(reserva.getId(), "Todo bien");

        assertEquals(EstadoReserva.COMPLETADA, completada.getEstado());
        assertEquals(EstadoChat.SOLO_LECTURA, completada.getEstadoChat());
        assertTrue(completada.isCalificacionHabilitada());
        assertEquals("Todo bien", completada.getComentarioCierre());
        verify(reservaRepository).save(any(ReservaEntity.class));

        EventoReserva evento = eventoPublicado();
        assertEquals(TipoEvento.RESERVA_COMPLETADA, evento.tipo());
        assertEquals(false, evento.payload().get("automatica"));
    }

    @Test
    void completar_sinComentario_debeGuardarComentarioNulo() {
        Reserva reserva = reservaConfirmada(1);
        when(reporteRepository.existsByReservaIdAndEstado(reserva.getId(), EstadoReporte.ABIERTO)).thenReturn(false);

        Reserva completada = reservaService.completar(reserva.getId(), "   ");
        assertEquals(EstadoReserva.COMPLETADA, completada.getEstado());
        assertNull(completada.getComentarioCierre());
    }

    @Test
    void completar_reservaNoExiste_debeLanzarNoEncontrada() {
        UUID inexistente = UUID.randomUUID();
        when(reservaRepository.findById(inexistente)).thenReturn(Optional.empty());

        assertThrows(ReservaNoEncontradaException.class, () -> reservaService.completar(inexistente, null));
        verify(reservaRepository, never()).save(any());
        verifyNoInteractions(observador);
    }

    @Test
    @DisplayName("Escenario 4: PENDIENTE, RECHAZADA o EXPIRADA no se pueden cerrar")
    void completar_reservaNoConfirmada_debeLanzarReglaDeNegocioSinGuardar() {
        for (EstadoReserva estado : List.of(EstadoReserva.PENDIENTE, EstadoReserva.RECHAZADA, EstadoReserva.EXPIRADA)) {
            UUID id = UUID.randomUUID();
            ReservaEntity entidad = ReservaEntity.builder()
                    .id(id)
                    .platoId(UUID.randomUUID()).cocineraId(COCINERA_ID).compradorId(COMPRADOR_ID)
                    .cantidadPorciones(2).montoTotal(new BigDecimal("32000"))
                    .estado(estado)
                    .fechaCreacion(LocalDateTime.now().minusMinutes(1))
                    .fechaLimiteConfirmacion(LocalDateTime.now().plusMinutes(9))
                    .build();
            Reserva dominio = Reserva.builder().id(id).estado(estado).build();
            when(reservaRepository.findById(id)).thenReturn(Optional.of(entidad));
            when(reservaEntityMapper.toDomain(any(ReservaEntity.class))).thenReturn(dominio);

            assertThrows(ReservaNoConfirmadaException.class,
                    () -> reservaService.completar(id, null));
        }
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Escenario 3: con un reporte ABIERTO el cierre se bloquea (422) y la reserva no cambia")
    void completar_conReporteAbierto_debeLanzarReglaDeNegocioSinCambios() {
        Reserva reserva = reservaConfirmada(1);
        when(reporteRepository.existsByReservaIdAndEstado(reserva.getId(), EstadoReporte.ABIERTO)).thenReturn(true);

        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class,
                () -> reservaService.completar(reserva.getId(), null));

        assertTrue(ex.getMessage().contains("reporte abierto"));
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(EstadoChat.ACTIVO, reserva.getEstadoChat());
        assertFalse(reserva.isCalificacionHabilitada());
        verify(reservaRepository, never()).save(any());
        verifyNoInteractions(observador);
    }

    @Test
    void buscarReservasParaCierreAutomatico_debeConsultarConfirmadasConLimiteDe24Horas() {
        UUID id = UUID.randomUUID();
        ReservaEntity vieja = ReservaEntity.builder()
                .id(id).estado(EstadoReserva.CONFIRMADA)
                .fechaDecision(LocalDateTime.now().minusHours(30)).build();
        when(reservaRepository.findByEstadoAndFechaDecisionLessThanEqual(
                eq(EstadoReserva.CONFIRMADA), any(LocalDateTime.class)))
                .thenReturn(List.of(vieja));

        List<UUID> ids = reservaService.buscarReservasParaCierreAutomatico();
        assertEquals(List.of(id), ids);
    }

    @Test
    @DisplayName("Escenario 2: a las 24 h sin cierre la reserva se completa sola")
    void completarAutomaticamente_a24Horas_debeCompletarYPublicarEventoAutomatico() {
        Reserva reserva = reservaConfirmada(25);
        when(reporteRepository.existsByReservaIdAndEstado(reserva.getId(), EstadoReporte.ABIERTO)).thenReturn(false);

        Reserva completada = reservaService.completarAutomaticamente(reserva.getId());

        assertEquals(EstadoReserva.COMPLETADA, completada.getEstado());
        assertEquals(EstadoChat.SOLO_LECTURA, completada.getEstadoChat());
        assertTrue(completada.isCalificacionHabilitada());

        EventoReserva evento = eventoPublicado();
        assertEquals(TipoEvento.RESERVA_COMPLETADA, evento.tipo());
        assertEquals(true, evento.payload().get("automatica"));
    }

    @Test
    void completarAutomaticamente_antesDe24Horas_noDebeHacerNada() {
        Reserva reserva = reservaConfirmada(23);

        Reserva resultado = reservaService.completarAutomaticamente(reserva.getId());

        assertEquals(EstadoReserva.CONFIRMADA, resultado.getEstado());
        verify(reservaRepository, never()).save(any());
        verifyNoInteractions(observador, reporteRepository);
    }

    @Test
    void completarAutomaticamente_conReporteAbierto_noDebeCompletar() {
        Reserva reserva = reservaConfirmada(48);
        when(reporteRepository.existsByReservaIdAndEstado(reserva.getId(), EstadoReporte.ABIERTO)).thenReturn(true);

        Reserva resultado = reservaService.completarAutomaticamente(reserva.getId());

        assertEquals(EstadoReserva.CONFIRMADA, resultado.getEstado());
        assertEquals(EstadoChat.ACTIVO, resultado.getEstadoChat());
        verify(reservaRepository, never()).save(any());
        verifyNoInteractions(observador);
    }
}