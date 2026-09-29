package com.ollacercana.service;

import com.ollacercana.domain.*;
import com.ollacercana.dto.response.ReservaResponseDTO;
import com.ollacercana.exception.*;
import com.ollacercana.mapper.ReservaMapper;
import com.ollacercana.repository.EventoReservaRepository;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReservaRepository;
import com.ollacercana.validator.ReservaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
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

    private ReservaValidator validator;
    private ReservaMapper reservaMapper;
    private ReservaServiceImpl reservaService;

    private final Long compradorId = 1L;
    private final UUID cocineraPerfilId = UUID.randomUUID();
    private final UUID platoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        validator = new ReservaValidator(reservaRepository, perfilCocineraRepository);
        reservaMapper = Mappers.getMapper(ReservaMapper.class);
        reservaService = new ReservaServiceImpl(
                reservaRepository,
                platoRepository,
                perfilCocineraRepository,
                eventoReservaRepository,
                validator,
                reservaMapper
        );

        lenient().when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> {
            Reserva r = i.getArgument(0);
            if (r.getId() == null) r.setId(UUID.randomUUID());
            return r;
        });

        lenient().when(platoRepository.saveAndFlush(any(Plato.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Plato platoMock(int totales, int comprometidas) {
        return Plato.builder()
                .id(platoId)
                .cocineraId(cocineraPerfilId)
                .nombre("Sancocho de Pollo")
                .precioPorcion(new BigDecimal("15000"))
                .porcionesTotales(totales)
                .porcionesComprometidas(comprometidas)
                .estado(EstadoPlato.ACTIVO)
                .version(0)
                .build();
    }

    @Test
    @DisplayName("1. Happy path: Crear reserva exitosa (201)")
    void crearReserva_Exitoso() {
        Plato plato = platoMock(5, 0);
        PerfilCocinera perfil = PerfilCocinera.builder().id(cocineraPerfilId).conjuntoResidencial("Torres del Parque").build();

        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));
        when(perfilCocineraRepository.findByCuentaId(compradorId)).thenReturn(Optional.empty()); // No es la misma cocinera
        when(reservaRepository.countByCompradorIdAndEstado(compradorId, EstadoReserva.PENDIENTE)).thenReturn(0L);
        when(perfilCocineraRepository.findById(cocineraPerfilId)).thenReturn(Optional.of(perfil));

        Reserva reserva = Reserva.builder()
                .platoId(platoId)
                .cantidadPorciones(2)
                .medioPago(MedioPago.NEQUI)
                .build();

        ReservaResponseDTO respuesta = reservaService.crear(compradorId, reserva);

        assertNotNull(respuesta);
        assertEquals(EstadoReserva.PENDIENTE, respuesta.getEstado());
        assertEquals(new BigDecimal("30000"), respuesta.getMonto());
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

        assertThrows(PlatoNoEncontradoException.class, () -> reservaService.crear(compradorId, reserva));
    }

    @Test
    @DisplayName("3. 409: Sin porciones suficientes (RN-03)")
    void crearReserva_SinPorciones_Lanza409() {
        Plato plato = platoMock(3, 3); // 0 disponibles
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));
        when(perfilCocineraRepository.findByCuentaId(compradorId)).thenReturn(Optional.empty());
        when(reservaRepository.countByCompradorIdAndEstado(compradorId, EstadoReserva.PENDIENTE)).thenReturn(0L);

        Reserva reserva = Reserva.builder().platoId(platoId).cantidadPorciones(1).build();

        assertThrows(PorcionesInsuficientesException.class, () -> reservaService.crear(compradorId, reserva));
    }

    @Test
    @DisplayName("4. 422: Cocinera intenta reservar su propio plato (RN-14)")
    void crearReserva_AutoReserva_Lanza422() {
        Plato plato = platoMock(5, 0);
        PerfilCocinera perfilMismaCocinera = PerfilCocinera.builder().id(cocineraPerfilId).build();

        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));
        // El comprador es dueño del perfil de la cocinera del plato
        when(perfilCocineraRepository.findByCuentaId(compradorId)).thenReturn(Optional.of(perfilMismaCocinera));

        Reserva reserva = Reserva.builder().platoId(platoId).cantidadPorciones(1).build();

        assertThrows(AutoReservaException.class, () -> reservaService.crear(compradorId, reserva));
    }

    @Test
    @DisplayName("5. 409: Comprador tiene 2 reservas pendientes (RN-15)")
    void crearReserva_LimitePendientesExcedido_Lanza409() {
        Plato plato = platoMock(5, 0);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));
        when(perfilCocineraRepository.findByCuentaId(compradorId)).thenReturn(Optional.empty());
        when(reservaRepository.countByCompradorIdAndEstado(compradorId, EstadoReserva.PENDIENTE)).thenReturn(2L);

        Reserva reserva = Reserva.builder().platoId(platoId).cantidadPorciones(1).build();

        assertThrows(LimiteReservasPendientesException.class, () -> reservaService.crear(compradorId, reserva));
    }

    @Test
    @DisplayName("6. Concurrencia: Conflicto optimista con @Version lanza ConflictoException (409)")
    void crearReserva_ColisionConcurrente_Lanza409() {
        Plato plato = platoMock(1, 0);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));
        when(perfilCocineraRepository.findByCuentaId(compradorId)).thenReturn(Optional.empty());
        when(reservaRepository.countByCompradorIdAndEstado(compradorId, EstadoReserva.PENDIENTE)).thenReturn(0L);
        when(platoRepository.saveAndFlush(any(Plato.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Plato.class, platoId));

        Reserva reserva = Reserva.builder().platoId(platoId).cantidadPorciones(1).build();

        assertThrows(ConflictoException.class, () -> reservaService.crear(compradorId, reserva));
    }
}