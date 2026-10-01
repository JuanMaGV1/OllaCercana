package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.controller.docs.ReservaApi;
import com.ollacercana.model.dto.response.ReservaResponseDTO;
import com.ollacercana.mapper.ReservaMapper;
import com.ollacercana.model.domain.EstadoReserva;
import com.ollacercana.model.domain.MedioPago;
import com.ollacercana.model.domain.Reserva;
import com.ollacercana.model.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.model.dto.request.ReservaRequestDTO;
import com.ollacercana.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservaController.class)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservaService reservaService;

    @MockBean
    private ReservaMapper reservaMapper;

    // ============ TEST: crear reserva ============

    @Test
    void crear_debeRetornar201() throws Exception {
        UUID platoId = UUID.randomUUID();
        ReservaRequestDTO request = new ReservaRequestDTO(
                platoId, 2, MedioPago.NEQUI, "Sin cebolla");

        ReservaResponseDTO response = new ReservaResponseDTO(
                UUID.randomUUID(), platoId, UUID.randomUUID(), 1L,
                2, new BigDecimal("30000.00"), MedioPago.NEQUI,
                EstadoReserva.PENDIENTE, "Sin cebolla",
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(10),
                null, null, null, null,
                false, null, null, null, false
        );

        when(reservaService.crear(eq(1L), any(ReservaRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/reservas")
                        .header("X-Comprador-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cantidadPorciones").value(2))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void crear_sinHeaderComprador_debeRetornar400() throws Exception {
        ReservaRequestDTO request = new ReservaRequestDTO(
                UUID.randomUUID(), 2, MedioPago.NEQUI, null);

        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ============ TEST: decidir reserva ============

    @Test
    void decidir_debeRetornar200() throws Exception {
        UUID reservaId = UUID.randomUUID();
        UUID cocineraId = UUID.randomUUID();

        DecisionReservaRequestDTO request = new DecisionReservaRequestDTO(
                com.ollacercana.model.domain.DecisionReserva.CONFIRMAR,
                LocalDateTime.now().plusHours(1),
                null, null
        );

        Reserva reserva = Reserva.builder()
                .id(reservaId)
                .platoId(UUID.randomUUID())
                .cocineraId(cocineraId)
                .compradorId(1L)
                .cantidadPorciones(2)
                .montoTotal(new BigDecimal("30000"))
                .medioPago(MedioPago.NEQUI)
                .estado(EstadoReserva.CONFIRMADA)
                .fechaCreacion(LocalDateTime.now())
                .fechaLimiteConfirmacion(LocalDateTime.now().plusMinutes(10))
                .chatHabilitado(true)
                .build();

        ReservaResponseDTO response = new ReservaResponseDTO(
                reservaId, reserva.getPlatoId(), cocineraId, 1L,
                2, new BigDecimal("30000"), MedioPago.NEQUI,
                EstadoReserva.CONFIRMADA, null,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(10),
                LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                null, null, true, null, null, null, false
        );

        when(reservaService.decidir(eq(reservaId), eq(cocineraId), any())).thenReturn(reserva);
        when(reservaMapper.toResponse(reserva)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/reservas/{id}/decision", reservaId)
                        .header("X-Cocinera-Id", cocineraId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

    // ============ TEST: listar pendientes ============

    @Test
    void listarPendientes_debeRetornar200() throws Exception {
        UUID cocineraId = UUID.randomUUID();

        when(reservaService.listarPendientesDeCocinera(cocineraId)).thenReturn(List.of());
        when(reservaMapper.toResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/reservas/pendientes")
                        .header("X-Cocinera-Id", cocineraId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ============ TEST: obtener por id ============

    @Test
    void obtenerPorId_debeRetornar200() throws Exception {
        UUID reservaId = UUID.randomUUID();
        Reserva reserva = Reserva.builder()
                .id(reservaId)
                .estado(EstadoReserva.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .fechaLimiteConfirmacion(LocalDateTime.now().plusMinutes(10))
                .build();

        ReservaResponseDTO response = new ReservaResponseDTO(
                reservaId, null, null, null,
                null, null, null, EstadoReserva.PENDIENTE,
                null, null, null, null, null, null, null,
                false, null, null, null, false
        );

        when(reservaService.obtenerPorId(reservaId)).thenReturn(reserva);
        when(reservaMapper.toResponse(reserva)).thenReturn(response);

        mockMvc.perform(get("/api/v1/reservas/{id}", reservaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservaId.toString()));
    }

    // ============ TEST: completar reserva ============

    @Test
    void completar_debeRetornar200() throws Exception {
        UUID reservaId = UUID.randomUUID();

        Reserva completada = Reserva.builder()
                .id(reservaId)
                .estado(EstadoReserva.COMPLETADA)
                .calificacionHabilitada(true)
                .fechaCompletada(LocalDateTime.now())
                .build();

        ReservaResponseDTO response = new ReservaResponseDTO(
                reservaId, null, null, null,
                null, null, null, EstadoReserva.COMPLETADA,
                null, null, null, LocalDateTime.now(), null, null, null,
                false, null, LocalDateTime.now(), "Todo bien", true
        );

        when(reservaService.completar(eq(reservaId), any())).thenReturn(completada);
        when(reservaMapper.toResponse(completada)).thenReturn(response);

        mockMvc.perform(post("/api/v1/reservas/{id}/completar", reservaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmacionEntrega\":true,\"confirmacionPago\":true,\"comentario\":\"Todo bien\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));
    }
}