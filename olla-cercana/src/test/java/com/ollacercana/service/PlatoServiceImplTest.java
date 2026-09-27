package com.ollacercana.service;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.Plato;
import com.ollacercana.domain.RestriccionAlimentaria;
import com.ollacercana.domain.TipoAjustePorciones;
import com.ollacercana.domain.TipoComida;
import com.ollacercana.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.dto.response.PlatoResponseDTO;
import com.ollacercana.exception.BusinessRuleException;
import com.ollacercana.exception.CantidadAjusteInvalidaException;
import com.ollacercana.exception.ConflictoVersionException;
import com.ollacercana.exception.PlatoNoEncontradoException;
import com.ollacercana.exception.ReduccionPorDebajoDeComprometidasException;
import com.ollacercana.mapper.PlatoDtoMapper;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.validator.CocineraQueryPort;
import com.ollacercana.validator.PlatoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas de PlatoServiceImpl (OC-94: crear / OC-110: ajustarDisponibilidad).
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
        PlatoDtoMapper mapper = new PlatoDtoMapper();
        PlatoValidator validator = new PlatoValidator(platoRepository, cocineraQueryPort);
        platoService = new PlatoServiceImpl(mapper, validator, platoRepository);

        lenient().when(platoRepository.save(any(Plato.class))).thenAnswer(invocation -> {
            Plato plato = invocation.getArgument(0);
            if (plato.getId() == null) {
                plato.setId(UUID.randomUUID());
            }
            return plato;
        });
    }

    private PlatoRequestDTO requestValido() {
        return new PlatoRequestDTO(
                "Bandeja paisa",
                "Bandeja paisa casera con frijoles, chicharrón y arroz",
                "https://fotos.ollacercana.com/bandeja.jpg",
                TipoComida.ALMUERZO,
                List.of(RestriccionAlimentaria.SIN_GLUTEN),
                5,
                new BigDecimal("15000"),
                LocalDateTime.now().plusHours(2),
                "Calle 80 #45-12, Bogotá",
                4.6789,
                -74.0567
        );
    }

    private Plato platoActivo(UUID id, int totales, int comprometidas, int version) {
        return Plato.builder()
                .id(id)
                .cocineraId(COCINERA_ID)
                .nombre("Bandeja paisa")
                .porcionesTotales(totales)
                .porcionesComprometidas(comprometidas)
                .precioPorcion(new BigDecimal("15000"))
                .estado(EstadoPlato.ACTIVO)
                .version(version)
                .build();
    }

    // ==================================================================
    // ============ crear() — OC-94 ============
    // ==================================================================

    // ============ Escenario 1: camino feliz ============

    @Test
    void crear_conCocineraVerificadaYDatosValidos_debePublicarPlato() {
        when(cocineraQueryPort.estaVerificada(COCINERA_ID)).thenReturn(true);
        when(cocineraQueryPort.estaPausada(COCINERA_ID)).thenReturn(false);
        when(platoRepository.countActivosVigentesPorCocinera(eq(COCINERA_ID), eq(EstadoPlato.ACTIVO), any()))
                .thenReturn(0L);

        PlatoResponseDTO response = platoService.crear(requestValido(), COCINERA_ID);

        assertNotNull(response.id());
        assertEquals(COCINERA_ID, response.cocineraId());
        assertEquals(EstadoPlato.ACTIVO, response.estado());
        assertNotNull(response.fechaPublicacion());
        assertNotNull(response.fechaExpiracion());

        // RN-02: vigencia de 4 horas
        Duration vigencia = Duration.between(response.fechaPublicacion(), response.fechaExpiracion());
        assertEquals(4, vigencia.toHours());

        verify(platoRepository, times(1)).save(any(Plato.class));
    }

    // ============ Escenario 2: cocinera no verificada / pausada ============

    @Test
    void crear_conCocineraNoVerificada_debeLanzarBusinessRuleException() {
        when(cocineraQueryPort.estaVerificada(COCINERA_ID)).thenReturn(false);

        assertThrows(BusinessRuleException.class,
                () -> platoService.crear(requestValido(), COCINERA_ID));

        verify(platoRepository, never()).save(any());
    }

    @Test
    void crear_conCocineraPausada_debeLanzarBusinessRuleException() {
        when(cocineraQueryPort.estaVerificada(COCINERA_ID)).thenReturn(true);
        when(cocineraQueryPort.estaPausada(COCINERA_ID)).thenReturn(true);

        assertThrows(BusinessRuleException.class,
                () -> platoService.crear(requestValido(), COCINERA_ID));

        verify(platoRepository, never()).save(any());
    }

    // ============ RN-27: precio fuera de rango ============

    @Test
    void crear_conPrecioMenorAlMinimo_debeLanzarBusinessRuleException() {
        when(cocineraQueryPort.estaVerificada(COCINERA_ID)).thenReturn(true);
        when(cocineraQueryPort.estaPausada(COCINERA_ID)).thenReturn(false);

        PlatoRequestDTO requestConPrecioInvalido = new PlatoRequestDTO(
                "Sopa", "Sopa casera de verduras frescas", "https://fotos.ollacercana.com/sopa.jpg",
                TipoComida.ALMUERZO, List.of(), 5,
                new BigDecimal("1000"), // menor a 2.000
                LocalDateTime.now().plusHours(1), "Calle 80 #45-12", 4.6789, -74.0567
        );

        assertThrows(BusinessRuleException.class,
                () -> platoService.crear(requestConPrecioInvalido, COCINERA_ID));

        verify(platoRepository, never()).save(any());
    }

    // ============ RN-28: máximo 3 platos activos por cocinera ============

    @Test
    void crear_conTresPlatosActivosVigentes_debeLanzarBusinessRuleException() {
        when(cocineraQueryPort.estaVerificada(COCINERA_ID)).thenReturn(true);
        when(cocineraQueryPort.estaPausada(COCINERA_ID)).thenReturn(false);
        when(platoRepository.countActivosVigentesPorCocinera(eq(COCINERA_ID), eq(EstadoPlato.ACTIVO), any()))
                .thenReturn(3L);

        assertThrows(BusinessRuleException.class,
                () -> platoService.crear(requestValido(), COCINERA_ID));

        verify(platoRepository, never()).save(any());
    }

    // ============ RN-30: máximo 3 restricciones alimentarias ============

    @Test
    void crear_conMasDeTresRestricciones_debeLanzarBusinessRuleException() {
        when(cocineraQueryPort.estaVerificada(COCINERA_ID)).thenReturn(true);
        when(cocineraQueryPort.estaPausada(COCINERA_ID)).thenReturn(false);
        when(platoRepository.countActivosVigentesPorCocinera(eq(COCINERA_ID), eq(EstadoPlato.ACTIVO), any()))
                .thenReturn(0L);

        PlatoRequestDTO requestConMuchasRestricciones = new PlatoRequestDTO(
                "Plato vegano", "Plato vegano sin gluten ni lácteos", "https://fotos.ollacercana.com/vegano.jpg",
                TipoComida.CENA,
                // Solo existen 3 valores en el enum — se repite uno para forzar tamaño 4.
                List.of(RestriccionAlimentaria.VEGETARIANO, RestriccionAlimentaria.VEGETARIANO,
                        RestriccionAlimentaria.SIN_GLUTEN, RestriccionAlimentaria.SIN_LACTOSA),
                5, new BigDecimal("15000"), LocalDateTime.now().plusHours(1),
                "Calle 80 #45-12", 4.6789, -74.0567
        );

        assertThrows(BusinessRuleException.class,
                () -> platoService.crear(requestConMuchasRestricciones, COCINERA_ID));

        verify(platoRepository, never()).save(any());
    }

    // ==================================================================
    // ============ ajustarDisponibilidad() — OC-110 / HU-24 ============
    // ==================================================================

    // ============ Escenario 1: aumentar ============

    @Test
    void ajustar_conAumentar_debeIncrementarTotalYVersion() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 3, 0, 0);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        var request = new AjusteDisponibilidadRequest(TipoAjustePorciones.AUMENTAR, 2, "reposición", 0);
        PlatoResponseDTO response = platoService.ajustarDisponibilidad(platoId, request);

        assertEquals(5, response.porcionesTotales());
        verify(platoRepository, times(1)).save(any(Plato.class));
    }

    // ============ Escenario 2: reducción por debajo de comprometidas ============

    @Test
    void ajustar_conReduccionPorDebajoDeComprometidas_debeLanzarExcepcion() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 3, 3, 0); // 3 comprometidas
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        // intenta dejar el total en 2, con 3 comprometidas → inválido
        var request = new AjusteDisponibilidadRequest(TipoAjustePorciones.DISMINUIR, 1, null, 0);

        assertThrows(ReduccionPorDebajoDeComprometidasException.class,
                () -> platoService.ajustarDisponibilidad(platoId, request));

        verify(platoRepository, never()).save(any());
    }

    // ============ Escenario 3: marcar agotado sin cancelar reservas ============

    @Test
    void ajustar_conMarcarAgotado_noDebeCancelarReservasExistentes() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 5, 3, 0); // 2 disponibles, 3 reservadas
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        var request = new AjusteDisponibilidadRequest(TipoAjustePorciones.MARCAR_AGOTADO, null, "cierre manual", 0);
        PlatoResponseDTO response = platoService.ajustarDisponibilidad(platoId, request);

        assertEquals(0, response.porcionesDisponibles());
        assertEquals(EstadoPlato.AGOTADO, response.estado());
        assertEquals(3, plato.getPorcionesComprometidas()); // reservas intactas
    }

    // ============ Escenario 4: conflicto de concurrencia ============

    @Test
    void ajustar_conVersionDesactualizada_debeLanzarConflictoVersion() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 3, 0, 5); // version actual = 5
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        var request = new AjusteDisponibilidadRequest(TipoAjustePorciones.AUMENTAR, 1, null, 4); // version vieja

        assertThrows(ConflictoVersionException.class,
                () -> platoService.ajustarDisponibilidad(platoId, request));

        verify(platoRepository, never()).save(any());
    }

    @Test
    void ajustar_conPlatoInexistente_debeLanzarPlatoNoEncontrado() {
        UUID platoId = UUID.randomUUID();
        when(platoRepository.findById(platoId)).thenReturn(Optional.empty());

        var request = new AjusteDisponibilidadRequest(TipoAjustePorciones.AUMENTAR, 1, null, 0);

        assertThrows(PlatoNoEncontradoException.class,
                () -> platoService.ajustarDisponibilidad(platoId, request));
    }

    @Test
    void ajustar_conCantidadInvalidaParaAumentar_debeLanzarExcepcion() {
        UUID platoId = UUID.randomUUID();
        Plato plato = platoActivo(platoId, 3, 0, 0);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        var request = new AjusteDisponibilidadRequest(TipoAjustePorciones.AUMENTAR, 0, null, 0);

        assertThrows(CantidadAjusteInvalidaException.class,
                () -> platoService.ajustarDisponibilidad(platoId, request));
    }
}