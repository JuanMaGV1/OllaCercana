package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.MedioPago;
import com.ollacercana.dto.request.ReservaRequestDTO;
import com.ollacercana.dto.response.ReservaResponseDTO;
import com.ollacercana.exception.AutoReservaException;
import com.ollacercana.exception.PorcionesInsuficientesException;
import com.ollacercana.service.IReservaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IReservaService reservaService;

    @Test
    @DisplayName("POST /api/v1/reservas - 201 Created con Comprador ID numérico")
    void crearReserva_Retorna201() throws Exception {
        Long compradorId = 1L;
        UUID platoId = UUID.randomUUID();

        ReservaRequestDTO request = new ReservaRequestDTO(
                platoId,
                2,
                MedioPago.NEQUI,
                "Llegaré puntual"
        );

        ReservaResponseDTO response = ReservaResponseDTO.builder()
                .id(UUID.randomUUID())
                .estado(EstadoReserva.PENDIENTE)
                .monto(new BigDecimal("30000.00"))
                .horaLimite(LocalDateTime.now().plusMinutes(10))
                .plato("Ajiaco Santafereño")
                .conjunto("Torres del Parque")
                .cantidadPorciones(2)
                .medioPago(MedioPago.NEQUI)
                .build();

        when(reservaService.crear(eq(compradorId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/reservas")
                        .header("X-Comprador-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.monto").value(30000.00))
                .andExpect(jsonPath("$.plato").value("Ajiaco Santafereño"));
    }

    @Test
    @DisplayName("POST /api/v1/reservas - 422 Unprocessable Entity si es auto-reserva (RN-14)")
    void crearReserva_AutoReserva_Retorna422() throws Exception {
        Long compradorId = 1L;
        UUID platoId = UUID.randomUUID();

        ReservaRequestDTO request = new ReservaRequestDTO(platoId, 1, MedioPago.NEQUI, null);

        when(reservaService.crear(eq(compradorId), any()))
                .thenThrow(new AutoReservaException());

        mockMvc.perform(post("/api/v1/reservas")
                        .header("X-Comprador-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /api/v1/reservas - 409 Conflict si no hay porciones disponibles")
    void crearReserva_SinPorciones_Retorna409() throws Exception {
        Long compradorId = 1L;
        UUID platoId = UUID.randomUUID();

        ReservaRequestDTO request = new ReservaRequestDTO(platoId, 5, MedioPago.NEQUI, null);

        when(reservaService.crear(eq(compradorId), any()))
                .thenThrow(new PorcionesInsuficientesException(0));

        mockMvc.perform(post("/api/v1/reservas")
                        .header("X-Comprador-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}