package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.domain.*;
import com.ollacercana.dto.request.CierreTransaccionRequestDTO;
import com.ollacercana.repository.NotificacionRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReporteRepository;
import com.ollacercana.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OC-160: POST /api/v1/reservas/{id}/completar — HU-23.
 * Las tareas programadas se apagan para que no interfieran con los datos de la prueba.
 */
@SpringBootTest(properties = "ollacercana.reservas.tareas-programadas=false")
@AutoConfigureMockMvc
class ReservaControllerCierreTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlatoRepository platoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    private Plato plato;
    private com.ollacercana.domain.Reserva reserva;

    @BeforeEach
    void setUp() {
        plato = platoRepository.save(Plato.builder()
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
                .build());

        LocalDateTime ahora = LocalDateTime.now();
        com.ollacercana.domain.Reserva nueva = com.ollacercana.domain.Reserva.crear(plato, 42L, 2, MedioPago.EFECTIVO, "Sin cebolla", ahora.minusMinutes(1));
        nueva.confirmar(ahora.plusMinutes(45), ahora);
        reserva = reservaRepository.save(nueva);
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

        com.ollacercana.domain.Reserva guardada = reservaRepository.findById(reserva.getId()).orElseThrow();
        assertEquals(EstadoReserva.COMPLETADA, guardada.getEstado());
        assertEquals(com.ollacercana.domain.EstadoChat.SOLO_LECTURA, guardada.getEstadoChat());
    }

    @Test
    @DisplayName("El cierre avisa a comprador y cocinera e invita a calificar")
    void completar_debeNotificarAAmbasPartes() throws Exception {
        completar(reserva.getId(), cierreValido()).andExpect(status().isOk());

        List<Notificacion> notificaciones = notificacionRepository.findByReservaIdOrderByFechaCreacionAsc(reserva.getId());
        assertEquals(2, notificaciones.size());
        assertTrue(notificaciones.stream().allMatch(n -> n.getTipo() == TipoNotificacion.INVITACION_CALIFICAR));
        assertTrue(notificaciones.stream().anyMatch(n -> n.getRolDestinatario() == Rol.COMPRADOR && Long.valueOf(42L).equals(n.getCompradorId())));
        assertTrue(notificaciones.stream().anyMatch(n -> n.getRolDestinatario() == Rol.COCINERA && plato.getCocineraId().equals(n.getCocineraId())));
    }

    @Test
    @DisplayName("Escenario 3: con un reporte ABIERTO el cierre se bloquea con 422")
    void completar_conReporteAbierto_debeRetornar422() throws Exception {
        reporteRepository.save(com.ollacercana.domain.Reporte.builder()
                .reservaId(reserva.getId())
                .estado(com.ollacercana.domain.EstadoReporte.ABIERTO)
                .fechaCreacion(LocalDateTime.now())
                .build());

        completar(reserva.getId(), cierreValido())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        com.ollacercana.domain.Reserva guardada = reservaRepository.findById(reserva.getId()).orElseThrow();
        assertEquals(EstadoReserva.CONFIRMADA, guardada.getEstado());
        assertEquals(com.ollacercana.domain.EstadoChat.ACTIVO, guardada.getEstadoChat());
    }

    @Test
    @DisplayName("Un reporte RESUELTO ya no bloquea el cierre")
    void completar_conReporteResuelto_debeRetornar200() throws Exception {
        reporteRepository.save(com.ollacercana.domain.Reporte.builder()
                .reservaId(reserva.getId())
                .estado(com.ollacercana.domain.EstadoReporte.RESUELTO)
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
