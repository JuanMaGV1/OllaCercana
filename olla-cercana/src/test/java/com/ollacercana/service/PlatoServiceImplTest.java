package com.ollacercana.service;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.Plato;
import com.ollacercana.domain.TipoComida;
import com.ollacercana.exception.CocineraNoEncontradaException;
import com.ollacercana.exception.LimitePlatosActivosExcedidoException;
import com.ollacercana.exception.PlatoNoEncontradoException;
import com.ollacercana.exception.PrecioFueraDeRangoException;
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

    @Mock
    private PlatoRepository platoRepository;

    @Mock
    private PlatoValidator validator;

    @Mock
    private com.ollacercana.repository.PerfilCocineraRepository perfilCocineraRepository;

    @InjectMocks
    private PlatoServiceImpl platoService;

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
        when(platoRepository.save(any(Plato.class))).thenAnswer(i -> {
            Plato p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        Plato resultado = platoService.crear(plato);

        assertNotNull(resultado.getId());
        assertEquals(EstadoPlato.ACTIVO, resultado.getEstado());
        verify(validator, times(1)).validarParaPublicar(any(Plato.class));
        verify(platoRepository, times(1)).save(plato);
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
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));
        assertEquals(plato, platoService.obtenerPorId(platoId));

        when(platoRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000000")))
                .thenReturn(Optional.empty());
        assertThrows(PlatoNoEncontradoException.class,
                () -> platoService.obtenerPorId(UUID.fromString("00000000-0000-0000-0000-000000000000")));
    }

    @Test
    @DisplayName("Eliminar - Elimina si existe o lanza 404 si no existe")
    void eliminar_evaluacion() {
        UUID platoId = UUID.randomUUID();
        when(platoRepository.existsById(platoId)).thenReturn(true);
        assertDoesNotThrow(() -> platoService.eliminar(platoId));
        verify(platoRepository).deleteById(platoId);

        when(platoRepository.existsById(platoId)).thenReturn(false);
        assertThrows(PlatoNoEncontradoException.class,
                () -> platoService.eliminar(platoId));
    }

    @Test
    @DisplayName("Buscar cercanos - Sin coordenadas de cliente no aplica filtro de distancia")
    void buscarCercanos_sinCoordenadas_retornaTodosLosActivos() {
        Plato plato = platoEjemplo();
        when(platoRepository.findActivosVigentes(eq(EstadoPlato.ACTIVO), any(LocalDateTime.class)))
                .thenReturn(List.of(plato));

        List<Plato> resultado = platoService.buscarCercanos(null, null);
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("OC-254: obtenerMediosPago retorna medios de pago si el perfil de la cocinera existe")
    void obtenerMediosPago_perfilExiste_retornaMediosPagoConfigurados() {
        Plato plato = platoEjemplo();
        when(platoRepository.findById(plato.getId())).thenReturn(Optional.of(plato));

        com.ollacercana.domain.PerfilCocinera perfil = com.ollacercana.domain.PerfilCocinera.builder()
                .id(COCINERA_ID)
                .mediosPago(List.of(com.ollacercana.domain.MedioPago.NEQUI, com.ollacercana.domain.MedioPago.EFECTIVO))
                .build();
        when(perfilCocineraRepository.findById(COCINERA_ID)).thenReturn(Optional.of(perfil));

        List<com.ollacercana.domain.MedioPago> medios = platoService.obtenerMediosPago(plato.getId());
        assertEquals(2, medios.size());
        assertTrue(medios.contains(com.ollacercana.domain.MedioPago.NEQUI));
        assertTrue(medios.contains(com.ollacercana.domain.MedioPago.EFECTIVO));
    }

    @Test
    @DisplayName("OC-254: obtenerMediosPago no falla y retorna lista vacía si el perfil de la cocinera no existe")
    void obtenerMediosPago_perfilNoExiste_retornaListaVacia() {
        Plato plato = platoEjemplo();
        when(platoRepository.findById(plato.getId())).thenReturn(Optional.of(plato));
        when(perfilCocineraRepository.findById(COCINERA_ID)).thenReturn(Optional.empty());

        List<com.ollacercana.domain.MedioPago> medios = platoService.obtenerMediosPago(plato.getId());
        assertNotNull(medios);
        assertTrue(medios.isEmpty());
    }
}