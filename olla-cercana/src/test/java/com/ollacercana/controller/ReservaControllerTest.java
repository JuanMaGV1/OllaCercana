package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.MedioPago;
import com.ollacercana.dto.request.ReservaRequestDTO;
import com.ollacercana.dto.response.ReservaResponseDTO;
import com.ollacercana.exception.AutoReservaException;
import com.ollacercana.exception.PorcionesInsuficientesException;
import com.ollacercana.service.IReservaService;
import com.ollacercana.domain.*;
import com.ollacercana.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.repository.NotificacionRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OC-150: PATCH /api/v1/reservas/{id}/decision — HU-12.
 * Las tareas programadas se apagan para que no interfieran con los datos de la prueba.
 */
@SpringBootTest(properties = "ollacercana.reservas.tareas-programadas=false")
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
    @Autowired
    private PlatoRepository platoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    private final UUID cocineraId = UUID.randomUUID();
    private Plato plato;
    private com.ollacercana.domain.Reserva reserva;

    @BeforeEach
    void setUp() {
        // Plato con 6 porciones, 2 ya comprometidas por la reserva pendiente
        plato = platoRepository.save(Plato.builder()
                .id(UUID.randomUUID())
                .cocineraId(cocineraId)
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

        reserva = reservaRepository.save(
                com.ollacercana.domain.Reserva.crear(plato, 42L, 2, MedioPago.NEQUI, "Sin cebolla", LocalDateTime.now().minusMinutes(1)));
    }

    private ResultActions decidir(UUID reservaId, UUID cocinera, DecisionReservaRequestDTO request) throws Exception {
        return mockMvc.perform(patch("/api/v1/reservas/{id}/decision", reservaId)
                .header("X-Cocinera-Id", cocinera.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private DecisionReservaRequestDTO confirmar() {
        return new DecisionReservaRequestDTO(com.ollacercana.domain.DecisionReserva.CONFIRMAR, LocalDateTime.now().plusMinutes(45), null, null);
    }

    private DecisionReservaRequestDTO rechazar(com.ollacercana.domain.MotivoRechazo motivo, String comentario) {
        return new DecisionReservaRequestDTO(com.ollacercana.domain.DecisionReserva.RECHAZAR, null, motivo, comentario);
    }

    @Test
    @DisplayName("Escenario 1: confirmar -> 200, CONFIRMADA, porciones conservadas y chat habilitado")
    void confirmar_debeRetornar200() throws Exception {
        decidir(reserva.getId(), cocineraId, confirmar())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reserva.getId().toString()))
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
                .andExpect(jsonPath("$.chatHabilitado").value(true))
                .andExpect(jsonPath("$.horaEstimadaEntrega").exists());

        Plato actualizado = platoRepository.findById(plato.getId()).orElseThrow();
        assertEquals(4, actualizado.getPorcionesDisponibles());
    }

    @Test
    @DisplayName("Escenario 2: rechazar -> 200, RECHAZADA y las porciones vuelven a la publicación")
    void rechazar_debeRetornar200YDevolverPorciones() throws Exception {
        decidir(reserva.getId(), cocineraId, rechazar(com.ollacercana.domain.MotivoRechazo.INGREDIENTES_INSUFICIENTES, null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"))
                .andExpect(jsonPath("$.motivoRechazo").value("INGREDIENTES_INSUFICIENTES"))
                .andExpect(jsonPath("$.chatHabilitado").value(false));

        Plato actualizado = platoRepository.findById(plato.getId()).orElseThrow();
        assertEquals(6, actualizado.getPorcionesDisponibles());
    }

    @Test
    @DisplayName("Escenario 3: el comprador recibe un aviso con la decisión")
    void decidir_debeGenerarNotificacionParaElComprador() throws Exception {
        decidir(reserva.getId(), cocineraId, rechazar(com.ollacercana.domain.MotivoRechazo.OTRO, "Se me dañó la estufa"))
                .andExpect(status().isOk());

        List<com.ollacercana.domain.Notificacion> notificaciones = notificacionRepository.findByReservaIdOrderByFechaCreacionAsc(reserva.getId());
        assertEquals(1, notificaciones.size());
        assertEquals(com.ollacercana.domain.TipoNotificacion.RESERVA_RECHAZADA, notificaciones.get(0).getTipo());
        assertEquals(Rol.COMPRADOR, notificaciones.get(0).getRolDestinatario());
        assertEquals(42L, notificaciones.get(0).getCompradorId());
    }

    @Test
    void rechazar_sinMotivo_debeRetornar400() throws Exception {
        decidir(reserva.getId(), cocineraId, rechazar(null, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.motivoValido").exists());
    }

    @Test
    void rechazar_conMotivoOtroSinComentario_debeRetornar400() throws Exception {
        decidir(reserva.getId(), cocineraId, rechazar(com.ollacercana.domain.MotivoRechazo.OTRO, " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.comentarioValido").exists());
    }

    @Test
    void rechazar_conComentarioDeMasDe150Caracteres_debeRetornar400() throws Exception {
        decidir(reserva.getId(), cocineraId, rechazar(com.ollacercana.domain.MotivoRechazo.OTRO, "x".repeat(151)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.comentario").exists());
    }

    @Test
    void confirmar_sinHoraEstimada_debeRetornar400() throws Exception {
        decidir(reserva.getId(), cocineraId, new DecisionReservaRequestDTO(com.ollacercana.domain.DecisionReserva.CONFIRMAR, null, null, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.horaEstimadaValida").exists());
    }

    @Test
    void decidir_conValorDeDecisionInvalido_debeRetornar400() throws Exception {
        mockMvc.perform(patch("/api/v1/reservas/{id}/decision", reserva.getId())
                        .header("X-Cocinera-Id", cocineraId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"ACEPTAR\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void decidir_sinHeaderDeCocinera_debeRetornar400() throws Exception {
        mockMvc.perform(patch("/api/v1/reservas/{id}/decision", reserva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmar())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void decidir_conCocineraAjena_debeRetornar403() throws Exception {
        decidir(reserva.getId(), UUID.randomUUID(), confirmar())
                .andExpect(status().isForbidden());
    }

    @Test
    void decidir_conReservaInexistente_debeRetornar404() throws Exception {
        decidir(UUID.randomUUID(), cocineraId, confirmar())
                .andExpect(status().isNotFound());
    }

    @Test
    void decidir_reservaYaGestionada_debeRetornar409() throws Exception {
        decidir(reserva.getId(), cocineraId, confirmar()).andExpect(status().isOk());

        decidir(reserva.getId(), cocineraId, rechazar(com.ollacercana.domain.MotivoRechazo.IMPREVISTO_PERSONAL, null))
                .andExpect(status().isConflict());
    }

    @Test
    void decidir_despuesDe10Minutos_debeRetornar409() throws Exception {
        com.ollacercana.domain.Reserva vencida = reservaRepository.save(
                com.ollacercana.domain.Reserva.crear(plato, 43L, 1, MedioPago.EFECTIVO, null, LocalDateTime.now().minusMinutes(11)));

        decidir(vencida.getId(), cocineraId, confirmar())
                .andExpect(status().isConflict());
    }

    @Test
    void listarPendientes_debeRetornarSoloLasVigentesDeLaCocinera() throws Exception {
        reservaRepository.save(
                com.ollacercana.domain.Reserva.crear(plato, 44L, 1, MedioPago.EFECTIVO, null, LocalDateTime.now().minusMinutes(15)));

        mockMvc.perform(get("/api/v1/reservas/pendientes").header("X-Cocinera-Id", cocineraId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(reserva.getId().toString()));
    }

    @Test
    void obtenerPorId_debeRetornar200() throws Exception {
        mockMvc.perform(get("/api/v1/reservas/{id}", reserva.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }
}
