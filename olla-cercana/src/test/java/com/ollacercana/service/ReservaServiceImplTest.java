package com.ollacercana.service;

import com.ollacercana.domain.*;
import com.ollacercana.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.exception.*;
import com.ollacercana.observer.ObservadorReserva;
import com.ollacercana.observer.PublicadorEventosReserva;
import com.ollacercana.repository.*;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private PlatoRepository platoRepository;

    @Mock
    private PerfilCocineraRepository perfilCocineraRepository;

    @Mock
    private EventoReservaRepository eventoReservaRepository;

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private ObservadorReserva observador;

    private ReservaValidator validator;
    private ReservaServiceImpl reservaService;

    private static final UUID COCINERA_ID = UUID.randomUUID();
    private static final Long COMPRADOR_ID = 42L;
    private final UUID platoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        validator = new ReservaValidator(reservaRepository, perfilCocineraRepository);
        PublicadorEventosReserva publicador = new PublicadorEventosReserva(List.of(observador));

        reservaService = new ReservaServiceImpl(
                reservaRepository,
                platoRepository,
                perfilCocineraRepository,
                reporteRepository,
                eventoReservaRepository,
                publicador,
                validator
        );

        lenient().when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> {
            Reserva r = i.getArgument(0);
            if (r.getId() == null) r.setId(UUID.randomUUID());
            return r;
        });

        lenient().when(reservaRepository.saveAndFlush(any(Reserva.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(platoRepository.saveAndFlush(any(Plato.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(platoRepository.save(any(Plato.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Plato platoMock(int totales, int comprometidas) {
        return Plato.builder()
                .id(platoId)
                .cocineraId(COCINERA_ID)
                .nombre("Sancocho de Pollo")
                .precioPorcion(new BigDecimal("15000"))
                .porcionesTotales(totales)
                .porcionesComprometidas(comprometidas)
                .estado(EstadoPlato.ACTIVO)
                .fechaExpiracion(LocalDateTime.now().plusHours(1))
                .build();
    }

    private Plato plato(int totales, int comprometidas, EstadoPlato estado) {
        return Plato.builder()
                .id(UUID.randomUUID())
                .cocineraId(COCINERA_ID)
                .nombre("Ajiaco santafereño")
                .porcionesTotales(totales)
                .porcionesComprometidas(comprometidas)
                .precioPorcion(new BigDecimal("16000"))
                .estado(estado)
                .fechaExpiracion(LocalDateTime.now().plusHours(1))
                .version(0)
                .build();
    }

    private Reserva reservaPendiente(Plato plato, int porciones, int minutosDesdeCreacion) {
        Reserva reserva = Reserva.crear(plato, COMPRADOR_ID, porciones, MedioPago.NEQUI, null,
                LocalDateTime.now().minusMinutes(minutosDesdeCreacion));
        lenient().when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
        return reserva;
    }

    private EventoReserva eventoPublicado() {
        ArgumentCaptor<EventoReserva> captor = ArgumentCaptor.forClass(EventoReserva.class);
        verify(observador, atLeastOnce()).notificar(captor.capture());
        return captor.getValue();
    }

                                                 
                                                            
                                                 

    @Test
    @DisplayName("1. Happy path: Crear reserva exitosa retorna dominio Reserva")
    void crearReserva_Exitoso() {
                  
        Plato plato = platoMock(5, 0);
        PerfilCocinera perfil = PerfilCocinera.builder().id(COCINERA_ID).conjuntoResidencial("Torres del Parque").build();

        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID)).thenReturn(Optional.empty());
        when(reservaRepository.countByCompradorIdAndEstado(COMPRADOR_ID, EstadoReserva.PENDIENTE)).thenReturn(0L);

        Reserva reserva = Reserva.builder()
                .platoId(platoId)
                .cantidadPorciones(2)
                .medioPago(MedioPago.NEQUI)
                .build();

              
        Reserva respuesta = reservaService.crear(COMPRADOR_ID, reserva);

                 
        assertNotNull(respuesta);
        assertEquals(EstadoReserva.PENDIENTE, respuesta.getEstado());
        assertEquals(new BigDecimal("30000"), respuesta.getMontoTotal());
        assertEquals(2, plato.getPorcionesComprometidas());
        assertEquals(3, plato.getPorcionesDisponibles());
        verify(reservaRepository).save(any(Reserva.class));
        verify(eventoReservaRepository).save(any(EventoReserva.class));
    }

    @Test
    @DisplayName("2. 404: Plato no existe")
    void crearReserva_PlatoNoExiste_Lanza404() {
        when(platoRepository.findById(platoId)).thenReturn(Optional.empty());

        Reserva reserva = Reserva.builder().platoId(platoId).cantidadPorciones(1).build();

        assertThrows(PlatoNoEncontradoException.class, () -> reservaService.crear(COMPRADOR_ID, reserva));
    }

    @Test
    @DisplayName("3. 409: Sin porciones suficientes (RN-03)")
    void crearReserva_SinPorciones_Lanza409() {
        Plato plato = platoMock(3, 3);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID)).thenReturn(Optional.empty());
        when(reservaRepository.countByCompradorIdAndEstado(COMPRADOR_ID, EstadoReserva.PENDIENTE)).thenReturn(0L);

        Reserva reserva = Reserva.builder().platoId(platoId).cantidadPorciones(1).build();

        assertThrows(PorcionesInsuficientesException.class, () -> reservaService.crear(COMPRADOR_ID, reserva));
    }

    @Test
    @DisplayName("4. 422: Cocinera intenta reservar su propio plato (RN-14)")
    void crearReserva_AutoReserva_Lanza422() {
        Plato plato = platoMock(5, 0);
        PerfilCocinera perfilMismaCocinera = PerfilCocinera.builder().id(COCINERA_ID).build();

        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID)).thenReturn(Optional.of(perfilMismaCocinera));

        Reserva reserva = Reserva.builder().platoId(platoId).cantidadPorciones(1).build();

        assertThrows(AutoReservaException.class, () -> reservaService.crear(COMPRADOR_ID, reserva));
    }

    @Test
    @DisplayName("5. 409: Comprador tiene 2 reservas pendientes (RN-15)")
    void crearReserva_LimitePendientesExcedido_Lanza409() {
        Plato plato = platoMock(5, 0);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID)).thenReturn(Optional.empty());
        when(reservaRepository.countByCompradorIdAndEstado(COMPRADOR_ID, EstadoReserva.PENDIENTE)).thenReturn(2L);

        Reserva reserva = Reserva.builder().platoId(platoId).cantidadPorciones(1).build();

        assertThrows(LimiteReservasPendientesException.class, () -> reservaService.crear(COMPRADOR_ID, reserva));
    }

    @Test
    @DisplayName("6. Concurrencia: Conflicto optimista con @Version lanza ConflictoException (409)")
    void crearReserva_ColisionConcurrente_Lanza409() {
        Plato plato = platoMock(1, 0);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID)).thenReturn(Optional.empty());
        when(reservaRepository.countByCompradorIdAndEstado(COMPRADOR_ID, EstadoReserva.PENDIENTE)).thenReturn(0L);
        when(platoRepository.saveAndFlush(any(Plato.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Plato.class, platoId));

        Reserva reserva = Reserva.builder().platoId(platoId).cantidadPorciones(1).build();

        assertThrows(ConflictoException.class, () -> reservaService.crear(COMPRADOR_ID, reserva));
    }

                                                 
                                                             
                                                 

    @Test
    void confirmar_debeCambiarAConfirmadaConservarPorcionesYHabilitarChat() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 2, 1);
        LocalDateTime horaEstimada = LocalDateTime.now().plusMinutes(40);

        Reserva confirmada = reservaService.confirmar(reserva.getId(), horaEstimada);

        assertEquals(EstadoReserva.CONFIRMADA, confirmada.getEstado());
        assertEquals(horaEstimada, confirmada.getHoraEstimadaEntrega());
        assertTrue(confirmada.isChatHabilitado());
        assertEquals(2, plato.getPorcionesComprometidas());
        verify(platoRepository, never()).save(any());
        verify(reservaRepository).saveAndFlush(reserva);

        EventoReserva evento = eventoPublicado();
        assertEquals(TipoEvento.RESERVA_CONFIRMADA, evento.tipo());
        assertEquals(COMPRADOR_ID, evento.compradorId());
        assertEquals(horaEstimada, evento.payload().get("horaEstimada"));
    }

    @Test
    void confirmar_reservaYaGestionada_debeLanzar409SinGuardar() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 2, 1);
        reserva.setEstado(EstadoReserva.RECHAZADA);
        UUID id = reserva.getId();
        LocalDateTime hora = LocalDateTime.now().plusHours(1);

        assertThrows(ReservaNoPendienteException.class, () -> reservaService.confirmar(id, hora));
        verify(reservaRepository, never()).saveAndFlush(any());
        verifyNoInteractions(observador);
    }

    @Test
    void confirmar_despuesDeLos10Minutos_debeLanzarReservaVencida() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 2, 11);
        UUID id = reserva.getId();
        LocalDateTime hora = LocalDateTime.now().plusHours(1);

        assertThrows(ReservaVencidaException.class, () -> reservaService.confirmar(id, hora));
        verify(reservaRepository, never()).saveAndFlush(any());
    }

    @Test
    void rechazar_debeCambiarARechazadaYDevolverLasPorcionesAlPlato() {
        Plato plato = plato(5, 5, EstadoPlato.AGOTADO);
        Reserva reserva = reservaPendiente(plato, 2, 1);
        when(platoRepository.findById(plato.getId())).thenReturn(Optional.of(plato));

        Reserva rechazada = reservaService.rechazar(reserva.getId(), MotivoRechazo.INGREDIENTES_INSUFICIENTES, null);

        assertEquals(EstadoReserva.RECHAZADA, rechazada.getEstado());
        assertEquals(MotivoRechazo.INGREDIENTES_INSUFICIENTES, rechazada.getMotivoRechazo());
        assertEquals(3, plato.getPorcionesComprometidas());
        assertEquals(2, plato.getPorcionesDisponibles());
        assertEquals(EstadoPlato.ACTIVO, plato.getEstado());
        verify(platoRepository).save(plato);

        EventoReserva evento = eventoPublicado();
        assertEquals(TipoEvento.RESERVA_RECHAZADA, evento.tipo());
        assertEquals(MotivoRechazo.INGREDIENTES_INSUFICIENTES.getDescripcion(), evento.payload().get("motivo"));
    }

    @Test
    void decidir_conCocineraAjena_debeLanzarAccesoDenegado() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 2, 1);
        UUID id = reserva.getId();
        UUID otraCocinera = UUID.randomUUID();
        var request = new DecisionReservaRequestDTO(DecisionReserva.CONFIRMAR, LocalDateTime.now().plusHours(1), null, null);

        assertThrows(AccesoDenegadoException.class, () -> reservaService.decidir(id, otraCocinera, request));
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void expirar_conHoraLimitePasada_debeExpirarYDevolverLasPorciones() {
        Plato plato = plato(4, 3, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 3, 11);
        when(platoRepository.findById(plato.getId())).thenReturn(Optional.of(plato));

        Reserva expirada = reservaService.expirar(reserva.getId());

        assertEquals(EstadoReserva.EXPIRADA, expirada.getEstado());
        assertEquals(0, plato.getPorcionesComprometidas());
        assertEquals(4, plato.getPorcionesDisponibles());
        verify(platoRepository).save(plato);
        assertEquals(TipoEvento.RESERVA_EXPIRADA, eventoPublicado().tipo());
    }

    @Test
    void enviarRecordatorio_aLos7MinutosSinRespuesta_debeNotificarALaCocinera() {
        Plato plato = plato(4, 2, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 2, 8);

        reservaService.enviarRecordatorio(reserva.getId());

        assertTrue(reserva.isRecordatorioEnviado());
        verify(reservaRepository).saveAndFlush(reserva);
        EventoReserva evento = eventoPublicado();
        assertEquals(TipoEvento.RECORDATORIO_RESERVA, evento.tipo());
        assertEquals(COCINERA_ID, evento.cocineraId());
        assertTrue(evento.payload().containsKey("minutosRestantes"));
    }

    @Test
    void listarPendientesDeCocinera_debeOmitirLasQueYaVencieron() {
        Plato plato = plato(6, 3, EstadoPlato.ACTIVO);
        Reserva vigente = Reserva.crear(plato, COMPRADOR_ID, 1, MedioPago.NEQUI, null, LocalDateTime.now().minusMinutes(2));
        Reserva vencida = Reserva.crear(plato, COMPRADOR_ID, 1, MedioPago.NEQUI, null, LocalDateTime.now().minusMinutes(15));
        when(reservaRepository.findByCocineraIdAndEstadoOrderByFechaLimiteConfirmacionAsc(COCINERA_ID, EstadoReserva.PENDIENTE))
                .thenReturn(List.of(vencida, vigente));

        List<Reserva> pendientes = reservaService.listarPendientesDeCocinera(COCINERA_ID);

        assertEquals(List.of(vigente), pendientes);
    }

    @Test
    void obtenerPorId_debeRetornarLaReserva() {
        Plato plato = plato(6, 1, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 1, 0);

        assertSame(reserva, reservaService.obtenerPorId(reserva.getId()));
    }

    @Test
    @DisplayName("Decidir - Flujo CONFIRMAR por switch")
    void decidir_confirmar_debeConfirmar() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 2, 1);
        LocalDateTime hora = LocalDateTime.now().plusHours(1);

        Reserva confirmada = reservaService.decidir(reserva.getId(), COCINERA_ID,
                new DecisionReservaRequestDTO(DecisionReserva.CONFIRMAR, hora, null, null));

        assertEquals(EstadoReserva.CONFIRMADA, confirmada.getEstado());
    }

    @Test
    @DisplayName("Expirar - Si no está vencida no altera la reserva")
    void expirar_noVencida_noHaceNada() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 2, 1);                             

        Reserva resultado = reservaService.expirar(reserva.getId());

        assertEquals(EstadoReserva.PENDIENTE, resultado.getEstado());
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Enviar recordatorio - Si no lo requiere aún, no lo envía")
    void enviarRecordatorio_noRequerido_noHaceNada() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 2, 1);                                        

        reservaService.enviarRecordatorio(reserva.getId());

        assertFalse(reserva.isRecordatorioEnviado());
        verify(reservaRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Buscar vencidas y para recordatorio consultan repositorio")
    void consultasProgramadas_ejecutanConsultas() {
        Reserva r = reservaPendiente(plato(5, 2, EstadoPlato.ACTIVO), 2, 1);
        when(reservaRepository.findByEstadoAndFechaLimiteConfirmacionLessThanEqual(any(), any()))
                .thenReturn(List.of(r));
        when(reservaRepository.findPendientesParaRecordatorio(any(), any(), any()))
                .thenReturn(List.of(r));

        assertEquals(List.of(r.getId()), reservaService.buscarReservasVencidas());
        assertEquals(List.of(r.getId()), reservaService.buscarReservasParaRecordatorio());
    }
}
