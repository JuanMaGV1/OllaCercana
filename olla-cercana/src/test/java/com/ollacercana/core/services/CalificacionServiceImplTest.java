package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.request.CalificacionRequestDTO;
import com.ollacercana.controller.dtos.response.CalificacionResponseDTO;
import com.ollacercana.controller.dtos.response.ResumenCalificacionesDTO;
import com.ollacercana.controller.handlers.exception.*;
import com.ollacercana.controller.mappers.CalificacionMapper;
import com.ollacercana.core.models.Calificacion;
import com.ollacercana.core.models.Reserva;
import com.ollacercana.core.models.enums.EstadoCalificacion;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.patterns.moderacion.EvaluadorReputacionCalificacion;
import com.ollacercana.core.services.impl.CalificacionServiceImpl;
import com.ollacercana.core.validators.CalificacionValidator;
import com.ollacercana.persistence.entities.CalificacionEntity;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.entities.ReservaEntity;
import com.ollacercana.persistence.mappers.CalificacionEntityMapper;
import com.ollacercana.persistence.mappers.ReservaEntityMapper;
import com.ollacercana.persistence.repository.CalificacionRepository;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalificacionServiceImplTest {

    @Mock private CalificacionRepository calificacionRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private PerfilCocineraRepository perfilRepository;
    @Mock private CalificacionEntityMapper calificacionEntityMapper;
    @Mock private ReservaEntityMapper reservaEntityMapper;
    @Mock private CalificacionMapper calificacionMapper;

    private CalificacionServiceImpl service;

    private static final UUID RESERVA_ID = UUID.randomUUID();
    private static final UUID COCINERA_ID = UUID.randomUUID();
    private static final Long COMPRADOR_ID = 42L;

    @BeforeEach
        void setUp() {
        CalificacionValidator validator = new CalificacionValidator(calificacionRepository);
        service = new CalificacionServiceImpl(
                calificacionRepository, reservaRepository, perfilRepository,
                calificacionEntityMapper, reservaEntityMapper,
                calificacionMapper, validator,
                new EvaluadorReputacionCalificacion(calificacionRepository, perfilRepository));
        }

    private Reserva reserva(EstadoReserva estado, Long compradorId) {
        return Reserva.builder()
                .id(RESERVA_ID)
                .compradorId(compradorId)
                .cocineraId(COCINERA_ID)
                .estado(estado)
                .build();
    }

    private CalificacionRequestDTO request(int estrellas, String comentario) {
        CalificacionRequestDTO r = new CalificacionRequestDTO();
        r.setEstrellas(estrellas);
        r.setComentario(comentario);
        return r;
    }

    @Test
    @DisplayName("HU-31: califica correctamente una reserva COMPLETADA")
    void calificar_exitoso() {
        Reserva r = reserva(EstadoReserva.COMPLETADA, COMPRADOR_ID);

        when(reservaRepository.findById(RESERVA_ID))
                .thenReturn(Optional.of(ReservaEntity.builder().id(RESERVA_ID).build()));
        when(reservaEntityMapper.toDomain(any(ReservaEntity.class))).thenReturn(r);

        when(calificacionRepository.findByReservaId(RESERVA_ID)).thenReturn(Optional.empty());

        CalificacionEntity entityGuardada = CalificacionEntity.builder()
                .id(UUID.randomUUID())
                .reservaId(RESERVA_ID)
                .cocineraId(COCINERA_ID)
                .compradorId(COMPRADOR_ID)
                .estrellas(5)
                .fechaCreacion(LocalDateTime.now())
                .build();
        when(calificacionEntityMapper.toEntity(any(Calificacion.class))).thenReturn(entityGuardada);
        when(calificacionRepository.save(entityGuardada)).thenReturn(entityGuardada);
        when(calificacionEntityMapper.toDomain(entityGuardada))
                .thenReturn(Calificacion.builder()
                        .id(entityGuardada.getId())
                        .reservaId(RESERVA_ID)
                        .cocineraId(COCINERA_ID)
                        .compradorId(COMPRADOR_ID)
                        .estrellas(5)
                        .build());

        CalificacionResponseDTO response = CalificacionResponseDTO.builder()
                .id(entityGuardada.getId())
                .estrellas(5)
                .build();
        when(calificacionMapper.toResponse(any(Calificacion.class))).thenReturn(response);

        when(perfilRepository.findById(COCINERA_ID)).thenReturn(Optional.empty());

        CalificacionResponseDTO result = service.calificar(RESERVA_ID, COMPRADOR_ID, request(5, "Excelente"));

        assertNotNull(result);
        assertEquals(5, result.getEstrellas());
        verify(calificacionRepository).save(any(CalificacionEntity.class));
    }

    @Test
    @DisplayName("HU-31: reserva inexistente → ReservaNoEncontradaException")
    void calificar_reservaNoExiste() {
        when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.empty());

        assertThrows(ReservaNoEncontradaException.class,
                () -> service.calificar(RESERVA_ID, COMPRADOR_ID, request(5, null)));
    }

    @Test
    @DisplayName("HU-31 / RN-31.1: otro comprador no puede calificar")
    void calificar_otroComprador() {
        Reserva r = reserva(EstadoReserva.COMPLETADA, 999L);

        when(reservaRepository.findById(RESERVA_ID))
                .thenReturn(Optional.of(ReservaEntity.builder().id(RESERVA_ID).build()));
        when(reservaEntityMapper.toDomain(any(ReservaEntity.class))).thenReturn(r);

        assertThrows(AccesoDenegadoCalificacionException.class,
                () -> service.calificar(RESERVA_ID, COMPRADOR_ID, request(5, null)));
    }

    @Test
    @DisplayName("HU-31 / RN-31.2: reserva no completada → ReservaNoCompletadaException")
    void calificar_reservaNoCompletada() {
        Reserva r = reserva(EstadoReserva.CONFIRMADA, COMPRADOR_ID);

        when(reservaRepository.findById(RESERVA_ID))
                .thenReturn(Optional.of(ReservaEntity.builder().id(RESERVA_ID).build()));
        when(reservaEntityMapper.toDomain(any(ReservaEntity.class))).thenReturn(r);

        assertThrows(ReservaNoCompletadaException.class,
                () -> service.calificar(RESERVA_ID, COMPRADOR_ID, request(5, null)));
    }

    @Test
    @DisplayName("HU-31 / RN-31.3: calificación duplicada → CalificacionDuplicadaException")
    void calificar_duplicada() {
        Reserva r = reserva(EstadoReserva.COMPLETADA, COMPRADOR_ID);

        when(reservaRepository.findById(RESERVA_ID))
                .thenReturn(Optional.of(ReservaEntity.builder().id(RESERVA_ID).build()));
        when(reservaEntityMapper.toDomain(any(ReservaEntity.class))).thenReturn(r);
        when(calificacionRepository.findByReservaId(RESERVA_ID))
                .thenReturn(Optional.of(CalificacionEntity.builder().id(UUID.randomUUID()).build()));

        assertThrows(CalificacionDuplicadaException.class,
                () -> service.calificar(RESERVA_ID, COMPRADOR_ID, request(5, null)));
    }

    @Test
    @DisplayName("HU-31: obtenerResumen con datos")
    void obtenerResumen_conDatos() {
        when(calificacionRepository.promedioPorCocinera(COCINERA_ID)).thenReturn(4.333);
        when(calificacionRepository.contarTotal(COCINERA_ID)).thenReturn(9L);
        when(calificacionRepository.contarPositivas(COCINERA_ID)).thenReturn(7L);

        ResumenCalificacionesDTO result = service.obtenerResumen(COCINERA_ID);

        assertEquals(4.33, result.getPromedio(), 0.001);
        assertEquals(9L, result.getTotal());
        assertEquals(7L, result.getPositivas());
    }

    @Test
    @DisplayName("HU-31: obtenerResumen sin datos")
    void obtenerResumen_sinDatos() {
        when(calificacionRepository.promedioPorCocinera(COCINERA_ID)).thenReturn(null);
        when(calificacionRepository.contarTotal(COCINERA_ID)).thenReturn(0L);
        when(calificacionRepository.contarPositivas(COCINERA_ID)).thenReturn(0L);

        ResumenCalificacionesDTO result = service.obtenerResumen(COCINERA_ID);

        assertNull(result.getPromedio());
        assertEquals(0L, result.getTotal());
        assertEquals(0L, result.getPositivas());
    }

    @Test
@DisplayName("HU-15: una calificación se guarda PENDIENTE, no publicada")
void calificar_guardaPendiente() {
    // GIVEN
    Reserva r = reserva(EstadoReserva.COMPLETADA, COMPRADOR_ID);

    when(reservaRepository.findById(RESERVA_ID))
            .thenReturn(Optional.of(ReservaEntity.builder().id(RESERVA_ID).build()));
    when(reservaEntityMapper.toDomain(any(ReservaEntity.class))).thenReturn(r);
    when(calificacionRepository.findByReservaId(RESERVA_ID)).thenReturn(Optional.empty());

    ArgumentCaptor<CalificacionEntity> captor = ArgumentCaptor.forClass(CalificacionEntity.class);

    // cuando el service llama a save(...), capturamos la entidad y devolvemos una con id asignado
    when(calificacionRepository.save(captor.capture())).thenAnswer(inv -> {
        CalificacionEntity e = inv.getArgument(0);
        if (e.getId() == null) e.setId(UUID.randomUUID());
        return e;
    });

    // el mapper entity→domain debe devolver un objeto NO NULO
    when(calificacionEntityMapper.toEntity(any(Calificacion.class))).thenAnswer(inv -> {
        Calificacion c = inv.getArgument(0);
        return CalificacionEntity.builder()
                .reservaId(c.getReservaId())
                .compradorId(c.getCompradorId())
                .cocineraId(c.getCocineraId())
                .estrellas(c.getEstrellas())
                .comentario(c.getComentario())
                .estado(c.getEstado())
                .fechaCreacion(c.getFechaCreacion())
                .fechaPublicacion(c.getFechaPublicacion())
                .fechaLimitePublicacion(c.getFechaLimitePublicacion())
                .build();
    });

    when(calificacionEntityMapper.toDomain(any(CalificacionEntity.class))).thenAnswer(inv -> {
        CalificacionEntity e = inv.getArgument(0);
        return Calificacion.builder()
                .id(e.getId())
                .reservaId(e.getReservaId())
                .compradorId(e.getCompradorId())
                .cocineraId(e.getCocineraId())
                .estrellas(e.getEstrellas())
                .comentario(e.getComentario())
                .estado(e.getEstado())
                .fechaCreacion(e.getFechaCreacion())
                .fechaPublicacion(e.getFechaPublicacion())
                .fechaLimitePublicacion(e.getFechaLimitePublicacion())
                .build();
    });

    when(calificacionMapper.toResponse(any(Calificacion.class)))
            .thenReturn(CalificacionResponseDTO.builder().estrellas(5).build());

    when(perfilRepository.findById(COCINERA_ID)).thenReturn(Optional.empty());

    // WHEN
    service.calificar(RESERVA_ID, COMPRADOR_ID, request(5, null));

    // THEN
    CalificacionEntity guardada = captor.getValue();
    assertEquals(EstadoCalificacion.PENDIENTE, guardada.getEstado());
    assertNull(guardada.getFechaPublicacion());
    assertNotNull(guardada.getFechaLimitePublicacion());
}
}