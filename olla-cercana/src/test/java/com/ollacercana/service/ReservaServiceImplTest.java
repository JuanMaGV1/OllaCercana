package com.ollacercana.service;

import com.ollacercana.domain.*;
import com.ollacercana.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.exception.AccesoDenegadoException;
import com.ollacercana.exception.DecisionReservaInvalidaException;
import com.ollacercana.exception.ReservaModificadaException;
import com.ollacercana.exception.ReservaNoEncontradaException;
import com.ollacercana.exception.ReservaNoPendienteException;
import com.ollacercana.exception.ReservaVencidaException;
import com.ollacercana.observer.ObservadorReserva;
import com.ollacercana.observer.PublicadorEventosReserva;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * OC-151: pruebas unitarias de confirmación, rechazo, expiración y recordatorio — HU-12.
 */
@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private PlatoRepository platoRepository;

    @Mock
    private ObservadorReserva observador;

    private com.ollacercana.service.ReservaServiceImpl reservaService;

    private static final UUID COCINERA_ID = UUID.randomUUID();
    private static final Long COMPRADOR_ID = 42L;

    @BeforeEach
    void setUp() {
        PublicadorEventosReserva publicador = new PublicadorEventosReserva(List.of(observador));
        reservaService = new com.ollacercana.service.ReservaServiceImpl(reservaRepository, platoRepository, publicador);

        lenient().when(reservaRepository.saveAndFlush(any(com.ollacercana.domain.Reserva.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(platoRepository.save(any(Plato.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ============ Datos de prueba ============

    private Plato plato(int totales, int comprometidas, EstadoPlato estado) {
        return Plato.builder()
                .id(UUID.randomUUID())
                .cocineraId(COCINERA_ID)
                .nombre("Ajiaco santafereño")
                .porcionesTotales(totales)
                .porcionesComprometidas(comprometidas)
                .precioPorcion(new BigDecimal("16000"))
                .estado(estado)
                .version(0)
                .build();
    }

    /** Reserva PENDIENTE creada "hace" los minutos indicados. */
    private com.ollacercana.domain.Reserva reservaPendiente(Plato plato, int porciones, int minutosDesdeCreacion) {
        com.ollacercana.domain.Reserva reserva = com.ollacercana.domain.Reserva.crear(plato, COMPRADOR_ID, porciones, MedioPago.NEQUI, null,
                LocalDateTime.now().minusMinutes(minutosDesdeCreacion));
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
        return reserva;
    }

    private EventoReserva eventoPublicado() {
        ArgumentCaptor<EventoReserva> captor = ArgumentCaptor.forClass(EventoReserva.class);
        verify(observador).notificar(captor.capture());
        return captor.getValue();
    }

    // ============ OC-146: confirmar ============

    @Test
    void confirmar_debeCambiarAConfirmadaConservarPorcionesYHabilitarChat() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 2, 1);
        LocalDateTime horaEstimada = LocalDateTime.now().plusMinutes(40);

        com.ollacercana.domain.Reserva confirmada = reservaService.confirmar(reserva.getId(), horaEstimada);

        assertEquals(com.ollacercana.domain.EstadoReserva.CONFIRMADA, confirmada.getEstado());
        assertEquals(horaEstimada, confirmada.getHoraEstimadaEntrega());
        assertTrue(confirmada.isChatHabilitado());
        // Las porciones siguen descontadas: no se toca el plato
        assertEquals(2, plato.getPorcionesComprometidas());
        verify(platoRepository, never()).save(any());
        verify(reservaRepository).saveAndFlush(reserva);

        EventoReserva evento = eventoPublicado();
        assertEquals(com.ollacercana.domain.TipoEvento.RESERVA_CONFIRMADA, evento.tipo());
        assertEquals(COMPRADOR_ID, evento.compradorId());
        assertEquals(horaEstimada, evento.payload().get("horaEstimada"));
    }

    @Test
    void confirmar_reservaYaGestionada_debeLanzar409SinGuardar() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 2, 1);
        reserva.setEstado(com.ollacercana.domain.EstadoReserva.RECHAZADA);
        UUID reservaId = reserva.getId();
        LocalDateTime hora = LocalDateTime.now().plusHours(1);

        assertThrows(ReservaNoPendienteException.class, () -> reservaService.confirmar(reservaId, hora));
        verify(reservaRepository, never()).saveAndFlush(any());
        verifyNoInteractions(observador);
    }

    @Test
    void confirmar_despuesDeLos10Minutos_debeLanzarReservaVencida() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 2, 11);
        UUID reservaId = reserva.getId();
        LocalDateTime hora = LocalDateTime.now().plusHours(1);

        assertThrows(ReservaVencidaException.class, () -> reservaService.confirmar(reservaId, hora));
        verify(reservaRepository, never()).saveAndFlush(any());
    }

    @Test
    void confirmar_conReservaInexistente_debeLanzarNoEncontrada() {
        UUID reservaId = UUID.randomUUID();
        LocalDateTime hora = LocalDateTime.now().plusHours(1);
        when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());

        assertThrows(ReservaNoEncontradaException.class, () -> reservaService.confirmar(reservaId, hora));
    }

    @Test
    void confirmar_conConflictoDeVersion_debeLanzarReservaModificada() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 2, 1);
        UUID reservaId = reserva.getId();
        LocalDateTime hora = LocalDateTime.now().plusHours(1);
        when(reservaRepository.saveAndFlush(any(com.ollacercana.domain.Reserva.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(com.ollacercana.domain.Reserva.class, reservaId));

        assertThrows(ReservaModificadaException.class, () -> reservaService.confirmar(reservaId, hora));
        verifyNoInteractions(observador);
    }

    // ============ OC-147: rechazar ============

    @Test
    void rechazar_debeCambiarARechazadaYDevolverLasPorcionesAlPlato() {
        Plato plato = plato(5, 5, EstadoPlato.AGOTADO); // agotado por reservas
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 2, 1);
        when(platoRepository.findById(plato.getId())).thenReturn(Optional.of(plato));

        com.ollacercana.domain.Reserva rechazada = reservaService.rechazar(reserva.getId(), com.ollacercana.domain.MotivoRechazo.INGREDIENTES_INSUFICIENTES, null);

        assertEquals(com.ollacercana.domain.EstadoReserva.RECHAZADA, rechazada.getEstado());
        assertEquals(com.ollacercana.domain.MotivoRechazo.INGREDIENTES_INSUFICIENTES, rechazada.getMotivoRechazo());
        // RN-03: las porciones vuelven y el plato se reactiva
        assertEquals(3, plato.getPorcionesComprometidas());
        assertEquals(2, plato.getPorcionesDisponibles());
        assertEquals(EstadoPlato.ACTIVO, plato.getEstado());
        verify(platoRepository).save(plato);

        EventoReserva evento = eventoPublicado();
        assertEquals(com.ollacercana.domain.TipoEvento.RESERVA_RECHAZADA, evento.tipo());
        assertEquals(com.ollacercana.domain.MotivoRechazo.INGREDIENTES_INSUFICIENTES.getDescripcion(), evento.payload().get("motivo"));
        assertFalse(evento.payload().containsKey("comentario"));
    }

    @Test
    void rechazar_conMotivoOtroYComentario_debeGuardarElComentario() {
        Plato plato = plato(5, 1, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 1, 2);
        when(platoRepository.findById(plato.getId())).thenReturn(Optional.of(plato));

        com.ollacercana.domain.Reserva rechazada = reservaService.rechazar(reserva.getId(), com.ollacercana.domain.MotivoRechazo.OTRO, "Se me dañó la estufa");

        assertEquals("Se me dañó la estufa", rechazada.getComentarioRechazo());
        assertEquals(0, plato.getPorcionesComprometidas());
        assertEquals("Se me dañó la estufa", eventoPublicado().payload().get("comentario"));
    }

    @Test
    void rechazar_conMotivoOtroSinComentario_noDebeTocarLasPorciones() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 2, 1);
        UUID reservaId = reserva.getId();

        assertThrows(DecisionReservaInvalidaException.class,
                () -> reservaService.rechazar(reservaId, com.ollacercana.domain.MotivoRechazo.OTRO, null));

        assertEquals(com.ollacercana.domain.EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(2, plato.getPorcionesComprometidas());
        verify(platoRepository, never()).save(any());
        verifyNoInteractions(observador);
    }

    @Test
    void rechazar_conPlatoEliminado_debeRechazarIgual() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 2, 1);
        when(platoRepository.findById(plato.getId())).thenReturn(Optional.empty());

        com.ollacercana.domain.Reserva rechazada = reservaService.rechazar(reserva.getId(), com.ollacercana.domain.MotivoRechazo.IMPREVISTO_PERSONAL, null);

        assertEquals(com.ollacercana.domain.EstadoReserva.RECHAZADA, rechazada.getEstado());
        verify(platoRepository, never()).save(any());
    }

    // ============ OC-150: decidir (lo que usa el endpoint) ============

    @Test
    void decidir_conCocineraAjena_debeLanzarAccesoDenegado() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 2, 1);
        UUID reservaId = reserva.getId();
        UUID otraCocinera = UUID.randomUUID();
        var request = new DecisionReservaRequestDTO(com.ollacercana.domain.DecisionReserva.CONFIRMAR, LocalDateTime.now().plusHours(1), null, null);

        assertThrows(AccesoDenegadoException.class, () -> reservaService.decidir(reservaId, otraCocinera, request));
        assertEquals(com.ollacercana.domain.EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void decidir_conConfirmar_debeConfirmar() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 2, 1);
        var request = new DecisionReservaRequestDTO(com.ollacercana.domain.DecisionReserva.CONFIRMAR, LocalDateTime.now().plusHours(1), null, null);

        com.ollacercana.domain.Reserva resultado = reservaService.decidir(reserva.getId(), COCINERA_ID, request);

        assertEquals(com.ollacercana.domain.EstadoReserva.CONFIRMADA, resultado.getEstado());
    }

    @Test
    void decidir_conRechazar_debeRechazarYDevolverPorciones() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 2, 1);
        when(platoRepository.findById(plato.getId())).thenReturn(Optional.of(plato));
        var request = new DecisionReservaRequestDTO(com.ollacercana.domain.DecisionReserva.RECHAZAR, null, com.ollacercana.domain.MotivoRechazo.SIN_TIEMPO_DE_ENTREGA, null);

        com.ollacercana.domain.Reserva resultado = reservaService.decidir(reserva.getId(), COCINERA_ID, request);

        assertEquals(com.ollacercana.domain.EstadoReserva.RECHAZADA, resultado.getEstado());
        assertEquals(0, plato.getPorcionesComprometidas());
    }

    // ============ OC-148: expiración automática ============

    @Test
    void expirar_conHoraLimitePasada_debeExpirarYDevolverLasPorciones() {
        Plato plato = plato(4, 3, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 3, 11); // hora límite: hace 1 minuto
        when(platoRepository.findById(plato.getId())).thenReturn(Optional.of(plato));

        com.ollacercana.domain.Reserva expirada = reservaService.expirar(reserva.getId());

        assertEquals(com.ollacercana.domain.EstadoReserva.EXPIRADA, expirada.getEstado());
        assertEquals(0, plato.getPorcionesComprometidas());
        assertEquals(4, plato.getPorcionesDisponibles());
        verify(platoRepository).save(plato);
        assertEquals(com.ollacercana.domain.TipoEvento.RESERVA_EXPIRADA, eventoPublicado().tipo());
    }

    @Test
    void expirar_reservaAunVigente_noDebeModificarla() {
        Plato plato = plato(4, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 2, 3);

        com.ollacercana.domain.Reserva resultado = reservaService.expirar(reserva.getId());

        assertEquals(com.ollacercana.domain.EstadoReserva.PENDIENTE, resultado.getEstado());
        verify(reservaRepository, never()).saveAndFlush(any());
        verifyNoInteractions(platoRepository, observador);
    }

    @Test
    void expirar_reservaQueYaFueConfirmada_noDebeModificarla() {
        Plato plato = plato(4, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 2, 11);
        reserva.setEstado(com.ollacercana.domain.EstadoReserva.CONFIRMADA);

        com.ollacercana.domain.Reserva resultado = reservaService.expirar(reserva.getId());

        assertEquals(com.ollacercana.domain.EstadoReserva.CONFIRMADA, resultado.getEstado());
        verifyNoInteractions(platoRepository, observador);
    }

    @Test
    void buscarReservasVencidas_debeRetornarLosIdsDePendientesVencidas() {
        Plato plato = plato(4, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva vencida = com.ollacercana.domain.Reserva.crear(plato, COMPRADOR_ID, 2, MedioPago.EFECTIVO, null,
                LocalDateTime.now().minusMinutes(12));
        when(reservaRepository.findByEstadoAndFechaLimiteConfirmacionLessThanEqual(eq(com.ollacercana.domain.EstadoReserva.PENDIENTE), any()))
                .thenReturn(List.of(vencida));

        assertEquals(List.of(vencida.getId()), reservaService.buscarReservasVencidas());
    }

    // ============ OC-149: recordatorio a los 7 minutos ============

    @Test
    void enviarRecordatorio_aLos7MinutosSinRespuesta_debeNotificarALaCocinera() {
        Plato plato = plato(4, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 2, 8);

        reservaService.enviarRecordatorio(reserva.getId());

        assertTrue(reserva.isRecordatorioEnviado());
        verify(reservaRepository).saveAndFlush(reserva);
        EventoReserva evento = eventoPublicado();
        assertEquals(com.ollacercana.domain.TipoEvento.RECORDATORIO_RESERVA, evento.tipo());
        assertEquals(COCINERA_ID, evento.cocineraId());
        assertTrue(evento.payload().containsKey("minutosRestantes"));
    }

    @Test
    void enviarRecordatorio_siYaSeEnvio_noDebeRepetirlo() {
        Plato plato = plato(4, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 2, 8);
        reserva.marcarRecordatorioEnviado();

        reservaService.enviarRecordatorio(reserva.getId());

        verify(reservaRepository, never()).saveAndFlush(any());
        verifyNoInteractions(observador);
    }

    @Test
    void enviarRecordatorio_siLaCocineraYaRespondio_noDebeEnviarlo() {
        Plato plato = plato(4, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 2, 8);
        reserva.setEstado(com.ollacercana.domain.EstadoReserva.CONFIRMADA);

        reservaService.enviarRecordatorio(reserva.getId());

        verifyNoInteractions(observador);
    }

    @Test
    void buscarReservasParaRecordatorio_debeConsultarLasCreadasHace7MinutosOMas() {
        Plato plato = plato(4, 2, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva candidata = com.ollacercana.domain.Reserva.crear(plato, COMPRADOR_ID, 1, MedioPago.NEQUI, null,
                LocalDateTime.now().minusMinutes(8));
        when(reservaRepository.findPendientesParaRecordatorio(eq(com.ollacercana.domain.EstadoReserva.PENDIENTE), any(), any()))
                .thenReturn(List.of(candidata));

        assertEquals(List.of(candidata.getId()), reservaService.buscarReservasParaRecordatorio());
    }

    // ============ Consultas ============

    @Test
    void listarPendientesDeCocinera_debeOmitirLasQueYaVencieron() {
        Plato plato = plato(6, 3, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva vigente = com.ollacercana.domain.Reserva.crear(plato, COMPRADOR_ID, 1, MedioPago.NEQUI, null, LocalDateTime.now().minusMinutes(2));
        com.ollacercana.domain.Reserva vencida = com.ollacercana.domain.Reserva.crear(plato, COMPRADOR_ID, 1, MedioPago.NEQUI, null, LocalDateTime.now().minusMinutes(15));
        when(reservaRepository.findByCocineraIdAndEstadoOrderByFechaLimiteConfirmacionAsc(COCINERA_ID, com.ollacercana.domain.EstadoReserva.PENDIENTE))
                .thenReturn(List.of(vencida, vigente));

        List<com.ollacercana.domain.Reserva> pendientes = reservaService.listarPendientesDeCocinera(COCINERA_ID);

        assertEquals(List.of(vigente), pendientes);
    }

    @Test
    void obtenerPorId_debeRetornarLaReserva() {
        Plato plato = plato(6, 1, EstadoPlato.ACTIVO);
        com.ollacercana.domain.Reserva reserva = reservaPendiente(plato, 1, 0);

        assertSame(reserva, reservaService.obtenerPorId(reserva.getId()));
    }
}
