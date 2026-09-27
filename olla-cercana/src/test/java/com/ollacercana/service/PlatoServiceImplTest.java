package com.ollacercana.service;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.Plato;
import com.ollacercana.domain.TipoComida;
import com.ollacercana.exception.CocineraNoEncontradaException;
import com.ollacercana.exception.CocineraPausadaException;
import com.ollacercana.exception.PrecioFueraDeRangoException;
import com.ollacercana.exception.LimitePlatosActivosExcedidoException;
import com.ollacercana.mapper.PlatoEntityMapper;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.validator.CocineraQueryPort;
import com.ollacercana.validator.PlatoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * OC-94: pruebas unitarias de PlatoServiceImpl.crear() — 5 escenarios.
 * Las pruebas de ajustarDisponibilidad() viven en su propia clase, no aquí.
 */
@ExtendWith(MockitoExtension.class)
class PlatoServiceImplTest {

    @Mock
    private CocineraQueryPort cocineraQueryPort;

    @Mock
    private PlatoRepository platoRepository;

    private PlatoServiceImpl platoService;

    private static final UUID COCINERA_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        PlatoEntityMapper entityMapper = Mappers.getMapper(PlatoEntityMapper.class);
        PlatoValidator validator = new PlatoValidator(platoRepository, cocineraQueryPort);
        platoService = new PlatoServiceImpl(platoRepository, entityMapper, validator);

        lenient().when(platoRepository.save(any(Plato.class))).thenAnswer(invocation -> {
            Plato plato = invocation.getArgument(0);
            if (plato.getId() == null) {
                plato.setId(UUID.randomUUID());
            }
            return plato;
        });
    }

    private Plato platoValido() {
        return Plato.builder()
                .cocineraId(COCINERA_ID)
                .nombre("Bandeja paisa")
                .descripcion("Bandeja paisa casera con frijoles, chicharrón y arroz")
                .fotoUrl("https://fotos.ollacercana.com/bandeja.jpg")
                .tipoComida(TipoComida.ALMUERZO)
                .restricciones(List.of())
                .porcionesTotales(5)
                .precioPorcion(new BigDecimal("15000"))
                .horaDisponibilidad(LocalDateTime.now().plusHours(2))
                .puntoEntrega("Calle 80 #45-12, Bogotá")
                .latitud(4.6789)
                .longitud(-74.0567)
                .build();
    }

    // ============ Escenario 1: happy path (201) ============

    @Test
    void crear_conDatosValidosYCocineraHabilitada_debePublicarPlato() {
        when(cocineraQueryPort.estaVerificada(COCINERA_ID)).thenReturn(true);
        when(cocineraQueryPort.estaPausada(COCINERA_ID)).thenReturn(false);
        when(platoRepository.countActivosVigentesPorCocinera(eq(COCINERA_ID), eq(EstadoPlato.ACTIVO), any()))
                .thenReturn(0L);

        Plato guardado = platoService.crear(platoValido());

        assertNotNull(guardado.getId());
        assertEquals(EstadoPlato.ACTIVO, guardado.getEstado());
        assertEquals(0, guardado.getPorcionesComprometidas());
        verify(platoRepository, times(1)).save(any(Plato.class));
    }

    // ============ Escenario 2: 404 — cocinera no encontrada ============

    @Test
    void crear_conCocineraInexistente_debeLanzarCocineraNoEncontrada() {
        when(cocineraQueryPort.estaVerificada(COCINERA_ID))
                .thenThrow(new CocineraNoEncontradaException(COCINERA_ID));

        assertThrows(CocineraNoEncontradaException.class,
                () -> platoService.crear(platoValido()));

        verify(platoRepository, never()).save(any());
    }

    // ============ Escenario 3: 409 — conflicto de regla (límite de 3 activos) ============

    @Test
    void crear_conTresPlatosActivosVigentes_debeLanzarConflicto() {
        when(cocineraQueryPort.estaVerificada(COCINERA_ID)).thenReturn(true);
        when(cocineraQueryPort.estaPausada(COCINERA_ID)).thenReturn(false);
        when(platoRepository.countActivosVigentesPorCocinera(eq(COCINERA_ID), eq(EstadoPlato.ACTIVO), any()))
                .thenReturn(3L);

        assertThrows(LimitePlatosActivosExcedidoException.class,
                () -> platoService.crear(platoValido()));

        verify(platoRepository, never()).save(any());
    }

    @Test
    void crear_conCocineraPausada_debeLanzarConflicto() {
        when(cocineraQueryPort.estaVerificada(COCINERA_ID)).thenReturn(true);
        when(cocineraQueryPort.estaPausada(COCINERA_ID)).thenReturn(true);

        assertThrows(CocineraPausadaException.class,
                () -> platoService.crear(platoValido()));

        verify(platoRepository, never()).save(any());
    }

    // ============ Escenario 4: 422 — estado/datos inválidos (precio fuera de rango) ============

    @Test
    void crear_conPrecioFueraDeRango_debeLanzarExcepcionDeValidacion() {
        when(cocineraQueryPort.estaVerificada(COCINERA_ID)).thenReturn(true);
        when(cocineraQueryPort.estaPausada(COCINERA_ID)).thenReturn(false);

        Plato platoConPrecioInvalido = platoValido().toBuilder()
                .precioPorcion(new BigDecimal("1000")) // menor al mínimo de 2.000
                .build();

        assertThrows(PrecioFueraDeRangoException.class,
                () -> platoService.crear(platoConPrecioInvalido));

        verify(platoRepository, never()).save(any());
    }

    // ============ Escenario 5: fecha de expiración = ahora + 4h (RN-02) ============

    @Test
    void crear_debeCalcularFechaExpiracionExactamente4hDespuesDeLaPublicacion() {
        when(cocineraQueryPort.estaVerificada(COCINERA_ID)).thenReturn(true);
        when(cocineraQueryPort.estaPausada(COCINERA_ID)).thenReturn(false);
        when(platoRepository.countActivosVigentesPorCocinera(eq(COCINERA_ID), eq(EstadoPlato.ACTIVO), any()))
                .thenReturn(0L);

        Plato guardado = platoService.crear(platoValido());

        assertNotNull(guardado.getFechaPublicacion());
        assertNotNull(guardado.getFechaExpiracion());
        Duration vigencia = Duration.between(guardado.getFechaPublicacion(), guardado.getFechaExpiracion());
        assertEquals(4, vigencia.toHours());
        assertEquals(0, vigencia.toMinutesPart());
    }
}
