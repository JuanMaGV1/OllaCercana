package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.core.models.*;
import com.ollacercana.core.models.enums.*;
import com.ollacercana.controller.dtos.request.*;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.entities.ReporteEntity;
import com.ollacercana.persistence.entities.ReservaEntity;
import com.ollacercana.persistence.repository.PlatoRepository;
import com.ollacercana.persistence.repository.ReporteRepository;
import com.ollacercana.persistence.repository.ReservaRepository;
import com.ollacercana.persistence.repository.mongo.NotificacionRepository;
import com.ollacercana.config.security.UsuarioActual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "ollacercana.reservas.tareas-programadas=false")
@AutoConfigureMockMvc
@WithMockUser(username = "42", roles = {"COMPRADOR"})
class ReservaControllerCierreTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PlatoRepository platoRepository;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private ReporteRepository reporteRepository;
    @Autowired private NotificacionRepository notificacionRepository;

    @MockBean private UsuarioActual usuarioActual;

    private PlatoEntity plato;
    private ReservaEntity reserva;

    @BeforeEach
    void setUp() {
        lenient().when(usuarioActual.getCuentaId()).thenReturn(42L);
        lenient().when(usuarioActual.tieneRol(Rol.ADMIN)).thenReturn(false);

        // ✅ Entity JPA, no dominio
        plato = platoRepository.save(PlatoEntity.builder()
                .id(UUID.randomUUID())
                .cocineraId(UUID.randomUUID())
                .nombre("Ajiaco santafereño")
                .descripcion("Ajiaco con pollo, papa criolla y mazorca")
                .fotoUrl("https://fotos.ollacercana.com/ajiaco.jpg")
                .tipoComida(TipoComida.ALMUERZO)
                .restricciones(List.of())
                .porcionesTotales(6)
                .porcionesComprometidas(2)
                .precioPorcion(new BigDecimal("16000"))
                .estado(EstadoPlato.ACTIVO)
                .fechaPublicacion(LocalDateTime.now().minusMinutes(20))
                .fechaExpiracion(LocalDateTime.now().plusHours(3))
                .puntoEntrega("Portería Torre 1")
                .latitud(4.6789)
                .longitud(-74.0567)
                .version(0)
                .build());

        lenient().when(usuarioActual.getCocineraId()).thenReturn(plato.getCocineraId());

        // ✅ ReservaEntity — armada con el builder, no con el dominio
        LocalDateTime ahora = LocalDateTime.now();
        reserva = reservaRepository.save(ReservaEntity.builder()
                .id(UUID.randomUUID())
                .platoId(plato.getId())
                .cocineraId(plato.getCocineraId())
                .compradorId(42L)
                .cantidadPorciones(2)
                .montoTotal(new BigDecimal("32000"))
                .medioPago(MedioPago.EFECTIVO)
                .estado(EstadoReserva.CONFIRMADA)
                .estadoChat(EstadoChat.ACTIVO)
                .chatHabilitado(true)
                .notaComprador("Sin cebolla")
                .fechaCreacion(ahora.minusMinutes(1))
                .fechaLimiteConfirmacion(ahora.plusMinutes(9))
                .fechaDecision(ahora)
                .version(0)
                .build());
    }

    private ResultActions completar(UUID reservaId, Object body) throws Exception {
        return mockMvc.perform(post("/api/v1/reservas/{id}/completar", reservaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private CierreTransaccionRequestDTO cierreValido() {
        return new CierreTransaccionRequestDTO(true, true, "Todo llegó caliente");
    }

    @Test
    @DisplayName("Escenario 1: 200, COMPLETADA, chat en SOLO_LECTURA y calificación habilitada")
    void completar_debeRetornar200() throws Exception {
        completar(reserva.getId(), cierreValido())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reserva.getId().toString()))
                .andExpect(jsonPath("$.estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.estadoChat").value("SOLO_LECTURA"))
                .andExpect(jsonPath("$.calificacionHabilitada").value(true))
                .andExpect(jsonPath("$.comentarioCierre").value("Todo llegó caliente"))
                .andExpect(jsonPath("$.fechaCompletada").exists());

        ReservaEntity guardada = reservaRepository.findById(reserva.getId()).orElseThrow();
        assertEquals(EstadoReserva.COMPLETADA, guardada.getEstado());
        assertEquals(EstadoChat.SOLO_LECTURA, guardada.getEstadoChat());
    }

    @Test
    @DisplayName("Escenario 3: con un reporte ABIERTO el cierre se bloquea con 422")
    void completar_conReporteAbierto_debeRetornar422() throws Exception {
        reporteRepository.save(ReporteEntity.builder()
        .id(UUID.randomUUID())
        .reservaId(reserva.getId())
        .objetivo(ObjetivoReporte.PLATO)
        .objetivoId(plato.getId())
        .motivo(MotivoReporte.CONTENIDO_INAPROPIADO)
        .reportanteId(1L)
        .estado(EstadoReporte.ABIERTO)
        .fechaCreacion(LocalDateTime.now())
        .build());

        completar(reserva.getId(), cierreValido())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        ReservaEntity guardada = reservaRepository.findById(reserva.getId()).orElseThrow();
        assertEquals(EstadoReserva.CONFIRMADA, guardada.getEstado());
        assertEquals(EstadoChat.ACTIVO, guardada.getEstadoChat());
    }

    @Test
    @DisplayName("Un reporte RESUELTO ya no bloquea el cierre")
    void completar_conReporteResuelto_debeRetornar200() throws Exception {
        reporteRepository.save(ReporteEntity.builder()
        .id(UUID.randomUUID())
        .reservaId(reserva.getId())
        .objetivo(ObjetivoReporte.PLATO)
        .objetivoId(plato.getId())
        .motivo(MotivoReporte.CONTENIDO_INAPROPIADO)
        .reportanteId(1L)
        .estado(EstadoReporte.RESUELTO)
        .fechaCreacion(LocalDateTime.now())
        .build());

        completar(reserva.getId(), cierreValido())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));
    }

    @Test
    @DisplayName("Escenario 4: una reserva no confirmada se rechaza con 422")
    void completar_reservaNoConfirmada_debeRetornar422() throws Exception {
        reserva.setEstado(EstadoReserva.RECHAZADA);
        reservaRepository.save(reserva);

        completar(reserva.getId(), cierreValido())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void completar_reservaInexistente_debeRetornar404() throws Exception {
        completar(UUID.randomUUID(), cierreValido())
                .andExpect(status().isNotFound());
    }

    @Test
    void completar_sinConfirmarEntrega_debeRetornar400() throws Exception {
        completar(reserva.getId(), new CierreTransaccionRequestDTO(false, true, null))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completar_sinConfirmarPago_debeRetornar400() throws Exception {
        completar(reserva.getId(), new CierreTransaccionRequestDTO(true, false, null))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completar_conCamposFaltantes_debeRetornar400() throws Exception {
        completar(reserva.getId(), java.util.Map.of("comentario", "hola"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completar_comentarioMayorA150_debeRetornar400() throws Exception {
        completar(reserva.getId(), new CierreTransaccionRequestDTO(true, true, "x".repeat(151)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completar_sinComentario_debeRetornar200() throws Exception {
        completar(reserva.getId(), new CierreTransaccionRequestDTO(true, true, null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));
    }
}