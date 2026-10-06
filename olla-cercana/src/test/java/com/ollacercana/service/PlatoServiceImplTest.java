package com.ollacercana.service;

import com.ollacercana.exception.*;
import com.ollacercana.mapper.PlatoEntityMapper;
import com.ollacercana.model.domain.EstadoPlato;
import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.domain.TipoComida;
import com.ollacercana.persistence.entity.PlatoEntity;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.service.impl.PlatoServiceImpl;
import com.ollacercana.validator.PlatoValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
class PlatoServiceImplTest {

    @Mock private PlatoRepository platoRepository;
    @Mock private PlatoValidator validator;
    @Mock private PlatoEntityMapper entityMapper;

    @InjectMocks private PlatoServiceImpl platoService;

    private static final UUID COCINERA_ID = UUID.randomUUID();

    private Plato platoEjemplo() {
        return Plato.builder()
                .cocineraId(COCINERA_ID)
                .nombre("Bandeja paisa")
                .descripcion("Frijoles campesinos con chicharrón")
                .tipoComida(TipoComida.ALMUERZO)
                .porcionesTotales(5)
                .precioPorcion(new BigDecimal("15000"))
                .puntoEntrega("Portería Torre 1")
                .latitud(4.6789)
                .longitud(-74.0567)
                .build();
    }

    @Test
    @DisplayName("Escenario 1: Happy path - Guardar plato correctamente")
    void crear_conDatosValidos_debePublicarPlato() {
        Plato plato = platoEjemplo();
        PlatoEntity entityGuardada = PlatoEntity.builder()
                .id(UUID.randomUUID()).nombre("Bandeja paisa").build();
        Plato dominioGuardado = platoEjemplo();
        dominioGuardado.setId(entityGuardada.getId());
        dominioGuardado.setEstado(EstadoPlato.ACTIVO);

        when(entityMapper.toEntity(any(Plato.class))).thenReturn(entityGuardada);
        when(platoRepository.save(any(PlatoEntity.class))).thenReturn(entityGuardada);
        when(entityMapper.toDomain(any(PlatoEntity.class))).thenReturn(dominioGuardado);

        Plato resultado = platoService.crear(plato);

        assertNotNull(resultado.getId());
        assertEquals(EstadoPlato.ACTIVO, resultado.getEstado());
        verify(validator).validarParaPublicar(any(Plato.class));
        verify(platoRepository).save(any(PlatoEntity.class));
    }

    @Test
    @DisplayName("Escenario 2: 404 - Cocinera inexistente lanza excepción")
    void crear_conCocineraInexistente_debeLanzarExcepcion() {
        Plato plato = platoEjemplo();
        doThrow(new CocineraNoEncontradaException(COCINERA_ID))
                .when(validator).validarParaPublicar(any(Plato.class));

        assertThrows(CocineraNoEncontradaException.class, () -> platoService.crear(plato));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Escenario 3: 409 - Límite de 3 platos activos alcanzado")
    void crear_conLimiteAlcanzado_debeLanzarConflicto() {
        Plato plato = platoEjemplo();
        doThrow(new LimitePlatosActivosExcedidoException(3))
                .when(validator).validarParaPublicar(any(Plato.class));

        assertThrows(LimitePlatosActivosExcedidoException.class, () -> platoService.crear(plato));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Escenario 4: 422 - Precio fuera del rango permitido")
    void crear_conPrecioInvalido_debeLanzarErrorDeValidacion() {
        Plato plato = platoEjemplo();
        doThrow(new PrecioFueraDeRangoException(new BigDecimal("2000"), new BigDecimal("50000")))
                .when(validator).validarParaPublicar(any(Plato.class));

        assertThrows(PrecioFueraDeRangoException.class, () -> platoService.crear(plato));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Escenario 5: Lista vacía - Retorna colección vacía cuando no hay coincidencias")
    void buscarCercanos_sinPlatos_debeRetornarListaVacia() {
        when(platoRepository.findActivosVigentes(eq(EstadoPlato.ACTIVO), any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<Plato> resultado = platoService.buscarCercanos(4.6789, -74.0567);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Obtener por Id - Exitoso cuando existe y 404 cuando no existe")
    void obtenerPorId_evaluacion() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoEjemplo();
        PlatoEntity entity = PlatoEntity.builder().id(platoId).build();

        when(platoRepository.findById(platoId)).thenReturn(Optional.of(entity));
        when(entityMapper.toDomain(entity)).thenReturn(plato);

        assertEquals(plato, platoService.obtenerPorId(platoId));

        UUID otro = UUID.randomUUID();
        when(platoRepository.findById(otro)).thenReturn(Optional.empty());
        assertThrows(PlatoNoEncontradoException.class, () -> platoService.obtenerPorId(otro));
    }

    @Test
    @DisplayName("Eliminar - Elimina si existe o lanza 404 si no existe")
    void eliminar_evaluacion() {
        UUID platoId = UUID.randomUUID();
        when(platoRepository.existsById(platoId)).thenReturn(true);
        assertDoesNotThrow(() -> platoService.eliminar(platoId));
        verify(platoRepository).deleteById(platoId);

        when(platoRepository.existsById(platoId)).thenReturn(false);
        assertThrows(PlatoNoEncontradoException.class, () -> platoService.eliminar(platoId));
    }

    @Test
    @DisplayName("Buscar cercanos - Sin coordenadas de cliente no aplica filtro de distancia")
    void buscarCercanos_sinCoordenadas_retornaTodosLosActivos() {
        Plato plato = platoEjemplo();
        PlatoEntity entity = PlatoEntity.builder().id(UUID.randomUUID()).build();

        when(platoRepository.findActivosVigentes(eq(EstadoPlato.ACTIVO), any(LocalDateTime.class)))
                .thenReturn(List.of(entity));
        when(entityMapper.toDomain(entity)).thenReturn(plato);

        List<Plato> resultado = platoService.buscarCercanos(null, null);
        assertEquals(1, resultado.size());
    }
}