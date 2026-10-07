package com.ollacercana.service;

import com.ollacercana.controller.dtos.request.DecisionReservaRequestDTO;
import com.ollacercana.controller.handlers.exception.*;
import com.ollacercana.core.models.EventoReserva;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.Reserva;
import com.ollacercana.core.models.enums.DecisionReserva;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.models.enums.MedioPago;
import com.ollacercana.core.models.enums.MotivoRechazo;
import com.ollacercana.core.models.enums.TipoEvento;
import com.ollacercana.core.patterns.observer.ObservadorReserva;
import com.ollacercana.core.patterns.observer.PublicadorEventosReserva;
import com.ollacercana.core.services.impl.ReservaServiceImpl;
import com.ollacercana.core.validators.ReservaValidator;
import com.ollacercana.persistence.document.EventoReservaDocument;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.entities.ReservaEntity;
import com.ollacercana.persistence.mappers.EventoMapper;
import com.ollacercana.persistence.mappers.PlatoEntityMapper;
import com.ollacercana.persistence.mappers.ReservaEntityMapper;
import com.ollacercana.persistence.repository.*;
import com.ollacercana.persistence.repository.mongo.EventoReservaRepository;

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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private PlatoRepository platoRepository;
    @Mock private PerfilCocineraRepository perfilCocineraRepository;
    @Mock private EventoReservaRepository eventoReservaRepository;
    @Mock private ReporteRepository reporteRepository;
    @Mock private ReservaValidator validator;
    @Mock private ReservaEntityMapper reservaEntityMapper;
    @Mock private PlatoEntityMapper platoEntityMapper;
    @Mock private EventoMapper eventoMapper;
    @Mock private ObservadorReserva observador;

    private ReservaServiceImpl reservaService;

    private static final UUID COCINERA_ID = UUID.randomUUID();
    private static final Long COMPRADOR_ID = 42L;
    private final UUID platoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        PublicadorEventosReserva publicador = new PublicadorEventosReserva(List.of(observador));

        reservaService = new ReservaServiceImpl(
                reservaRepository,
                platoRepository,
                perfilCocineraRepository,
                reporteRepository,
                eventoReservaRepository,
                publicador,
                validator,
                reservaEntityMapper,
                platoEntityMapper,
                eventoMapper);

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

        lenient().when(platoEntityMapper.toEntity(any(Plato.class))).thenAnswer(i -> {
            Plato p = i.getArgument(0);
            return PlatoEntity.builder()
                    .id(p.getId())
                    .cocineraId(p.getCocineraId())
                    .nombre(p.getNombre())
                    .porcionesTotales(p.getPorcionesTotales())
                    .porcionesComprometidas(p.getPorcionesComprometidas())
                    .precioPorcion(p.getPrecioPorcion())
                    .estado(p.getEstado())
                    .version(p.getVersion())
                    .build();
        });

        lenient().when(platoEntityMapper.toDomain(any(PlatoEntity.class))).thenAnswer(i -> {
            PlatoEntity e = i.getArgument(0);
            return Plato.builder()
                    .id(e.getId())
                    .cocineraId(e.getCocineraId())
                    .nombre(e.getNombre())
                    .porcionesTotales(e.getPorcionesTotales())
                    .porcionesComprometidas(e.getPorcionesComprometidas())
                    .precioPorcion(e.getPrecioPorcion())
                    .estado(e.getEstado())
                    .version(e.getVersion())
                    .build();
        });

        lenient().when(eventoMapper.toDocument(any(EventoReserva.class)))
                .thenReturn(EventoReservaDocument.builder().id(UUID.randomUUID().toString()).build());

        lenient().when(reservaRepository.save(any(ReservaEntity.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(reservaRepository.saveAndFlush(any(ReservaEntity.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(platoRepository.save(any(PlatoEntity.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(platoRepository.saveAndFlush(any(PlatoEntity.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(eventoReservaRepository.save(any(EventoReservaDocument.class)))
                .thenAnswer(i -> i.getArgument(0));
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
                .version(0)
                .build();
    }

    private ReservaEntity reservaEntityDe(Reserva r) {
        return ReservaEntity.builder()
                .id(r.getId())
                .platoId(r.getPlatoId())
                .cocineraId(r.getCocineraId())
                .compradorId(r.getCompradorId())
                .cantidadPorciones(r.getCantidadPorciones())
                .montoTotal(r.getMontoTotal())
                .medioPago(r.getMedioPago())
                .estado(r.getEstado())
                .fechaCreacion(r.getFechaCreacion())
                .fechaLimiteConfirmacion(r.getFechaLimiteConfirmacion())
                .fechaDecision(r.getFechaDecision())
                .build();
    }

    private Plato registrarPlato(Plato plato) {
        PlatoEntity entidad = PlatoEntity.builder()
                .id(plato.getId())
                .cocineraId(plato.getCocineraId())
                .nombre(plato.getNombre())
                .porcionesTotales(plato.getPorcionesTotales())
                .porcionesComprometidas(plato.getPorcionesComprometidas())
                .precioPorcion(plato.getPrecioPorcion())
                .estado(plato.getEstado())
                .version(plato.getVersion())
                .build();
        when(platoRepository.findById(plato.getId())).thenReturn(Optional.of(entidad));
        when(platoEntityMapper.toDomain(entidad)).thenReturn(plato);
        return plato;
    }

    private Reserva reservaPendiente(Plato plato, int porciones, int minutosDesdeCreacion) {
        Reserva reserva = Reserva.crear(plato, COMPRADOR_ID, porciones, MedioPago.NEQUI, null,
                LocalDateTime.now().minusMinutes(minutosDesdeCreacion));
        ReservaEntity entidad = reservaEntityDe(reserva);
        lenient().when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(entidad));
        lenient().when(reservaEntityMapper.toDomain(entidad)).thenReturn(reserva);
        return reserva;
    }

    private EventoReserva eventoPublicado() {
        ArgumentCaptor<EventoReserva> captor = ArgumentCaptor.forClass(EventoReserva.class);
        verify(observador, atLeastOnce()).notificar(captor.capture());
        return captor.getValue();
    }

    // ============ PRUEBAS DE CREACIÓN ============

    @Test
    @DisplayName("1. Happy path: Crear reserva exitosa retorna dominio Reserva")
    void crearReserva_Exitoso() {
        Plato plato = plato(5, 0, EstadoPlato.ACTIVO);
        registrarPlato(plato);

        // NOTA: no se stubean perfilCocineraRepository ni countBy...
        // porque validator.validarParaCrear ya cubre esas validaciones.

        Reserva reserva = Reserva.builder()
                .platoId(plato.getId())
                .cantidadPorciones(2)
                .medioPago(MedioPago.NEQUI)
                .build();

        Reserva respuesta = reservaService.crear(COMPRADOR_ID, reserva);

        assertNotNull(respuesta);
        assertEquals(EstadoReserva.PENDIENTE, respuesta.getEstado());
        assertEquals(new BigDecimal("32000"), respuesta.getMontoTotal());
        assertEquals(2, plato.getPorcionesComprometidas());
        assertEquals(3, plato.getPorcionesDisponibles());
        verify(reservaRepository).save(any(ReservaEntity.class));
        verify(platoRepository).saveAndFlush(any(PlatoEntity.class));
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
        Plato plato = plato(3, 3, EstadoPlato.ACTIVO);
        registrarPlato(plato);

        doThrow(new PorcionesInsuficientesException(0))
                .when(validator).validarParaCrear(eq(COMPRADOR_ID), any(Plato.class), eq(1));

        Reserva reserva = Reserva.builder().platoId(plato.getId()).cantidadPorciones(1).build();

        assertThrows(PorcionesInsuficientesException.class,
                () -> reservaService.crear(COMPRADOR_ID, reserva));
    }

    @Test
    @DisplayName("4. 422: Cocinera intenta reservar su propio plato (RN-14)")
    void crearReserva_AutoReserva_Lanza422() {
        Plato plato = plato(5, 0, EstadoPlato.ACTIVO);
        registrarPlato(plato);

        doThrow(new AutoReservaException())
                .when(validator).validarParaCrear(eq(COMPRADOR_ID), any(Plato.class), anyInt());

        Reserva reserva = Reserva.builder().platoId(plato.getId()).cantidadPorciones(1).build();

        assertThrows(AutoReservaException.class,
                () -> reservaService.crear(COMPRADOR_ID, reserva));
    }

    @Test
    @DisplayName("5. 409: Comprador tiene 2 reservas pendientes (RN-15)")
    void crearReserva_LimitePendientesExcedido_Lanza409() {
        Plato plato = plato(5, 0, EstadoPlato.ACTIVO);
        registrarPlato(plato);

        doThrow(new LimiteReservasPendientesException())
                .when(validator).validarParaCrear(eq(COMPRADOR_ID), any(Plato.class), anyInt());

        Reserva reserva = Reserva.builder().platoId(plato.getId()).cantidadPorciones(1).build();

        assertThrows(LimiteReservasPendientesException.class,
                () -> reservaService.crear(COMPRADOR_ID, reserva));
    }

    @Test
    @DisplayName("6. Concurrencia: Conflicto optimista lanza ConflictoException (409)")
    void crearReserva_ColisionConcurrente_Lanza409() {
        Plato plato = plato(1, 0, EstadoPlato.ACTIVO);
        registrarPlato(plato);

        when(platoRepository.saveAndFlush(any(PlatoEntity.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(PlatoEntity.class, plato.getId()));

        Reserva reserva = Reserva.builder().platoId(plato.getId()).cantidadPorciones(1).build();

        assertThrows(ConflictoException.class, () -> reservaService.crear(COMPRADOR_ID, reserva));
    }

    // ============ PRUEBAS DE CONFIRMACIÓN / RECHAZO ============

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
        verify(reservaRepository).save(any(ReservaEntity.class));

        EventoReserva evento = eventoPublicado();
        assertEquals(TipoEvento.RESERVA_CONFIRMADA, evento.tipo());
        assertEquals(COMPRADOR_ID, evento.compradorId());
    }

    @Test
    void confirmar_reservaYaGestionada_debeLanzar409SinGuardar() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 2, 1);
        reserva.setEstado(EstadoReserva.RECHAZADA);

        ReservaEntity entidad = reservaEntityDe(reserva);
        entidad.setEstado(EstadoReserva.RECHAZADA);
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(entidad));
        when(reservaEntityMapper.toDomain(entidad)).thenReturn(reserva);

        LocalDateTime hora = LocalDateTime.now().plusHours(1);

        assertThrows(ReservaNoPendienteException.class,
                () -> reservaService.confirmar(reserva.getId(), hora));
        verify(reservaRepository, never()).save(any(ReservaEntity.class));
    }

    @Test
    void confirmar_despuesDeLos10Minutos_debeLanzarReservaVencida() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 2, 11);
        LocalDateTime hora = LocalDateTime.now().plusHours(1);

        assertThrows(ReservaVencidaException.class,
                () -> reservaService.confirmar(reserva.getId(), hora));
        verify(reservaRepository, never()).save(any(ReservaEntity.class));
    }

    @Test
    void rechazar_debeCambiarARechazadaYDevolverLasPorcionesAlPlato() {
        Plato plato = plato(5, 5, EstadoPlato.AGOTADO);
        Reserva reserva = reservaPendiente(plato, 2, 1);
        registrarPlato(plato);

        Reserva rechazada = reservaService.rechazar(
                reserva.getId(), MotivoRechazo.INGREDIENTES_INSUFICIENTES, null);

        assertEquals(EstadoReserva.RECHAZADA, rechazada.getEstado());
        assertEquals(MotivoRechazo.INGREDIENTES_INSUFICIENTES, rechazada.getMotivoRechazo());
        assertEquals(3, plato.getPorcionesComprometidas());
        assertEquals(2, plato.getPorcionesDisponibles());
        assertEquals(EstadoPlato.ACTIVO, plato.getEstado());
        verify(platoRepository).save(any(PlatoEntity.class));

        EventoReserva evento = eventoPublicado();
        assertEquals(TipoEvento.RESERVA_RECHAZADA, evento.tipo());
    }

    @Test
    void decidir_conCocineraAjena_debeLanzarAccesoDenegado() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 2, 1);
        UUID otraCocinera = UUID.randomUUID();
        var request = new DecisionReservaRequestDTO(
                DecisionReserva.CONFIRMAR, LocalDateTime.now().plusHours(1), null, null);

        assertThrows(AccesoDenegadoException.class,
                () -> reservaService.decidir(reserva.getId(), otraCocinera, request));
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void expirar_conHoraLimitePasada_debeExpirarYDevolverLasPorciones() {
        Plato plato = plato(4, 3, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 3, 11);
        registrarPlato(plato);

        Reserva expirada = reservaService.expirar(reserva.getId());

        assertEquals(EstadoReserva.EXPIRADA, expirada.getEstado());
        assertEquals(0, plato.getPorcionesComprometidas());
        assertEquals(4, plato.getPorcionesDisponibles());
        verify(platoRepository).save(any(PlatoEntity.class));
        assertEquals(TipoEvento.RESERVA_EXPIRADA, eventoPublicado().tipo());
    }

    @Test
    void enviarRecordatorio_aLos7MinutosSinRespuesta_debeNotificarALaCocinera() {
        Plato plato = plato(4, 2, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 2, 8);

        reservaService.enviarRecordatorio(reserva.getId());

        assertTrue(reserva.isRecordatorioEnviado());
        verify(reservaRepository).save(any(ReservaEntity.class));
        EventoReserva evento = eventoPublicado();
        assertEquals(TipoEvento.RECORDATORIO_RESERVA, evento.tipo());
        assertEquals(COCINERA_ID, evento.cocineraId());
    }

    @Test
    void listarPendientesDeCocinera_debeOmitirLasQueYaVencieron() {
        Plato plato = plato(6, 3, EstadoPlato.ACTIVO);
        Reserva vigente = Reserva.crear(plato, COMPRADOR_ID, 1, MedioPago.NEQUI, null,
                LocalDateTime.now().minusMinutes(2));
        Reserva vencida = Reserva.crear(plato, COMPRADOR_ID, 1, MedioPago.NEQUI, null,
                LocalDateTime.now().minusMinutes(15));

        ReservaEntity vigenteE = reservaEntityDe(vigente);
        ReservaEntity vencidaE = reservaEntityDe(vencida);

        when(reservaRepository.findByCocineraIdAndEstadoOrderByFechaLimiteConfirmacionAsc(
                COCINERA_ID, EstadoReserva.PENDIENTE))
                .thenReturn(List.of(vencidaE, vigenteE));
        when(reservaEntityMapper.toDomain(vencidaE)).thenReturn(vencida);
        when(reservaEntityMapper.toDomain(vigenteE)).thenReturn(vigente);

        List<Reserva> pendientes = reservaService.listarPendientesDeCocinera(COCINERA_ID);

        assertEquals(1, pendientes.size());
        assertEquals(vigente.getId(), pendientes.get(0).getId());
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
        verify(platoRepository, never()).save(any(PlatoEntity.class));
    }

    @Test
    @DisplayName("Enviar recordatorio - Si no lo requiere aún, no lo envía")
    void enviarRecordatorio_noRequerido_noHaceNada() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        Reserva reserva = reservaPendiente(plato, 2, 1);

        reservaService.enviarRecordatorio(reserva.getId());

        assertFalse(reserva.isRecordatorioEnviado());
        verify(reservaRepository, never()).save(any(ReservaEntity.class));
    }

    @Test
    @DisplayName("Buscar vencidas y para recordatorio consultan repositorio")
    void consultasProgramadas_ejecutanConsultas() {
        Plato plato = plato(5, 2, EstadoPlato.ACTIVO);
        Reserva r = reservaPendiente(plato, 2, 1);
        ReservaEntity entidad = reservaEntityDe(r);

        when(reservaRepository.findByEstadoAndFechaLimiteConfirmacionLessThanEqual(any(), any()))
                .thenReturn(List.of(entidad));
        when(reservaRepository.findPendientesParaRecordatorio(any(), any(), any()))
                .thenReturn(List.of(entidad));

        assertEquals(List.of(r.getId()), reservaService.buscarReservasVencidas());
        assertEquals(List.of(r.getId()), reservaService.buscarReservasParaRecordatorio());
    }
}