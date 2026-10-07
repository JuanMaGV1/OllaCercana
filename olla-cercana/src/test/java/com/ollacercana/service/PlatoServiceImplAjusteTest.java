package com.ollacercana.service;

import com.ollacercana.controller.handlers.exception.*;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.TipoAjustePorciones;
import com.ollacercana.core.services.impl.PlatoServiceImpl;
import com.ollacercana.core.validators.PlatoValidator;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.mappers.PlatoEntityMapper;
import com.ollacercana.persistence.repository.PlatoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatoServiceImplAjusteTest {

    @Mock private PlatoRepository platoRepository;
    @Mock private PlatoValidator validator;
    @Mock private PlatoEntityMapper entityMapper;

    @InjectMocks private PlatoServiceImpl platoService;

    @BeforeEach
    void setUp() {
        // entityMapper.toDomain(entity) → devuelve el dominio que le pasamos por "side-channel"
        // Se usa thenAnswer para que Mockito reconstruya siempre un objeto consistente.
        lenient().when(entityMapper.toEntity(any(Plato.class))).thenAnswer(inv -> {
            Plato p = inv.getArgument(0);
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

        // Platos del mock: solo se registran los necesarios dentro de cada test.
    }

    private Plato platoActivo(UUID id, int totales, int comprometidas, int version) {
        return Plato.builder()
                .id(id)
                .cocineraId(UUID.randomUUID())
                .nombre("Bandeja paisa")
                .porcionesTotales(totales)
                .porcionesComprometidas(comprometidas)
                .precioPorcion(new BigDecimal("15000"))
                .estado(EstadoPlato.ACTIVO)
                .version(version)
                .build();
    }

    /**
     * Configura el mock de forma que:
     *   - findById(id)  → entity del plato ORIGINAL
     *   - entityMapper.toDomain(entity) → devuelve el dominio que se está mutando en el Service
     *   - saveAndFlush(entity) → devuelve el entity reflejando el dominio mutado
     *   - entityMapper.toDomain(any(PlatoEntity)) en el resultado → devuelve el plato mutado
     */
    private void registrarPlatoQueSeMuta(Plato platoOriginal) {
        // Mapa simple: id -> dominio original (mutable)
        when(platoRepository.findById(platoOriginal.getId()))
                .thenReturn(Optional.of(PlatoEntity.builder().id(platoOriginal.getId()).build()));
        when(entityMapper.toDomain(any(PlatoEntity.class))).thenAnswer(inv -> platoOriginal);
        when(platoRepository.saveAndFlush(any(PlatoEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void ajustar_conAumentar_debeIncrementarTotalYVersion() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 3, 0, 0);
        registrarPlatoQueSeMuta(plato);

        Plato actualizado = platoService.ajustarDisponibilidad(platoId, TipoAjustePorciones.AUMENTAR, 2, 0);

        // El Service muta el MISMO objeto que devolvió toDomain → por eso el resultado es 5.
        assertEquals(5, actualizado.getPorcionesTotales());
        verify(platoRepository, times(1)).saveAndFlush(any(PlatoEntity.class));
    }

    @Test
    void ajustar_conReduccionPorDebajoDeComprometidas_debeLanzarExcepcion() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 3, 3, 0);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(PlatoEntity.builder().id(platoId).build()));
        when(entityMapper.toDomain(any(PlatoEntity.class))).thenReturn(plato);

        doThrow(new ReduccionPorDebajoDeComprometidasException(3))
                .when(validator).validarAjusteDisponibilidad(plato, TipoAjustePorciones.DISMINUIR, 1);

        assertThrows(ReduccionPorDebajoDeComprometidasException.class,
                () -> platoService.ajustarDisponibilidad(platoId, TipoAjustePorciones.DISMINUIR, 1, 0));

        verify(platoRepository, never()).saveAndFlush(any(PlatoEntity.class));
    }

    @Test
    void ajustar_conMarcarAgotado_noDebeCancelarReservasExistentes() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 5, 3, 0);
        registrarPlatoQueSeMuta(plato);

        Plato actualizado = platoService.ajustarDisponibilidad(platoId, TipoAjustePorciones.MARCAR_AGOTADO, null, 0);

        assertEquals(0, actualizado.getPorcionesDisponibles());
        assertEquals(EstadoPlato.AGOTADO, actualizado.getEstado());
        assertEquals(3, actualizado.getPorcionesComprometidas());
    }

    @Test
    void ajustar_conVersionDesactualizada_debeLanzarConflictoVersion() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 3, 0, 5);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(PlatoEntity.builder().id(platoId).build()));
        when(entityMapper.toDomain(any(PlatoEntity.class))).thenReturn(plato);
        when(platoRepository.saveAndFlush(any(PlatoEntity.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(PlatoEntity.class, platoId));

        assertThrows(ConflictoVersionException.class,
                () -> platoService.ajustarDisponibilidad(platoId, TipoAjustePorciones.AUMENTAR, 1, 4));
    }

    @Test
    void ajustar_conPlatoInexistente_debeLanzarPlatoNoEncontrado() {
        UUID platoId = UUID.randomUUID();
        when(platoRepository.findById(platoId)).thenReturn(Optional.empty());

        assertThrows(PlatoNoEncontradoException.class,
                () -> platoService.ajustarDisponibilidad(platoId, TipoAjustePorciones.AUMENTAR, 1, 0));
    }

    @Test
    void ajustar_conCantidadInvalidaParaAumentar_debeLanzarExcepcion() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 3, 0, 0);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(PlatoEntity.builder().id(platoId).build()));
        when(entityMapper.toDomain(any(PlatoEntity.class))).thenReturn(plato);

        doThrow(new CantidadAjusteInvalidaException())
                .when(validator).validarAjusteDisponibilidad(plato, TipoAjustePorciones.AUMENTAR, 0);

        assertThrows(CantidadAjusteInvalidaException.class,
                () -> platoService.ajustarDisponibilidad(platoId, TipoAjustePorciones.AUMENTAR, 0, 0));
    }

    @Test
    void ajustar_conDisminuir_debeDecrementarTotalSinQuedarPorDebajoDeComprometidas() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 5, 2, 0);
        registrarPlatoQueSeMuta(plato);

        Plato actualizado = platoService.ajustarDisponibilidad(platoId, TipoAjustePorciones.DISMINUIR, 2, 0);

        assertEquals(3, actualizado.getPorcionesTotales());
        verify(platoRepository, times(1)).saveAndFlush(any(PlatoEntity.class));
    }
}