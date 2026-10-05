package com.ollacercana.service;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.Plato;
import com.ollacercana.domain.TipoAjustePorciones;
import com.ollacercana.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.exception.CantidadAjusteInvalidaException;
import com.ollacercana.exception.ConflictoVersionException;
import com.ollacercana.exception.PlatoNoEncontradoException;
import com.ollacercana.exception.ReduccionPorDebajoDeComprometidasException;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.service.impl.PlatoServiceImpl;
import com.ollacercana.validator.PlatoValidator;
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

    @Mock
    private PlatoRepository platoRepository;

    @Mock
    private PlatoValidator validator;

    @InjectMocks
    private PlatoServiceImpl platoService;

    @BeforeEach
    void setUp() {
        lenient().when(platoRepository.saveAndFlush(any(Plato.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
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

    @Test
    void ajustar_conAumentar_debeIncrementarTotalYVersion() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 3, 0, 0);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        Plato actualizado = platoService.ajustarDisponibilidad(platoId, TipoAjustePorciones.AUMENTAR, 2, 0);

        assertEquals(5, actualizado.getPorcionesTotales());
        verify(platoRepository, times(1)).saveAndFlush(any(Plato.class));
    }

    @Test
    void ajustar_conReduccionPorDebajoDeComprometidas_debeLanzarExcepcion() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 3, 3, 0);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        doThrow(new ReduccionPorDebajoDeComprometidasException(3))
                .when(validator).validarAjusteDisponibilidad(plato, TipoAjustePorciones.DISMINUIR, 1);

        assertThrows(ReduccionPorDebajoDeComprometidasException.class,
                () -> platoService.ajustarDisponibilidad(platoId, TipoAjustePorciones.DISMINUIR, 1, 0));

        verify(platoRepository, never()).saveAndFlush(any());
    }

    @Test
    void ajustar_conMarcarAgotado_noDebeCancelarReservasExistentes() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 5, 3, 0);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        Plato actualizado = platoService.ajustarDisponibilidad(platoId, TipoAjustePorciones.MARCAR_AGOTADO, null, 0);

        assertEquals(0, actualizado.getPorcionesDisponibles());
        assertEquals(EstadoPlato.AGOTADO, actualizado.getEstado());
        assertEquals(3, actualizado.getPorcionesComprometidas());
    }

    @Test
    void ajustar_conVersionDesactualizada_debeLanzarConflictoVersion() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 3, 0, 5);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));
        when(platoRepository.saveAndFlush(any(Plato.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Plato.class, platoId));

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
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        doThrow(new CantidadAjusteInvalidaException())
                .when(validator).validarAjusteDisponibilidad(plato, TipoAjustePorciones.AUMENTAR, 0);

        assertThrows(CantidadAjusteInvalidaException.class,
                () -> platoService.ajustarDisponibilidad(platoId, TipoAjustePorciones.AUMENTAR, 0, 0));
    }

    @Test
    void ajustar_conDisminuir_debeDecrementarTotalSinQuedarPorDebajoDeComprometidas() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 5, 2, 0);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        Plato actualizado = platoService.ajustarDisponibilidad(platoId, TipoAjustePorciones.DISMINUIR, 2, 0);

        assertEquals(3, actualizado.getPorcionesTotales());
        verify(platoRepository, times(1)).saveAndFlush(any(Plato.class));
    }
}