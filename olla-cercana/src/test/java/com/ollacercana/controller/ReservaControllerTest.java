package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.domain.*;
import com.ollacercana.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.dto.request.ReservaRequestDTO;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReservaRepository;
import com.ollacercana.repository.mongo.NotificacionRepository;
import com.ollacercana.security.UsuarioActual;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "ollacercana.reservas.tareas-programadas=false")
@AutoConfigureMockMvc
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlatoRepository platoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PerfilCocineraRepository perfilCocineraRepository;

    @Autowired(required = false)
    private NotificacionRepository notificacionRepository;

    @MockBean
    private UsuarioActual usuarioActual;

    private final UUID cocineraId = UUID.randomUUID();
    private Plato plato;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();
        platoRepository.deleteAll();
        perfilCocineraRepository.deleteAll();

        lenient().when(usuarioActual.getCuentaId()).thenReturn(42L);
        lenient().when(usuarioActual.getCocineraId()).thenReturn(cocineraId);
        lenient().when(usuarioActual.tieneRol(any())).thenReturn(false);

        PerfilCocinera perfilCocinera = PerfilCocinera.builder()
                .id(cocineraId)
                .conjuntoResidencial("Torres del Parque")
                .verificada(true)
                .build();
        perfilCocineraRepository.save(perfilCocinera);

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
                Reserva.crear(plato, 42L, 2, MedioPago.NEQUI, "Sin cebolla", LocalDateTime.now().minusMinutes(1)));
    }

    private ResultActions decidir(UUID reservaId, DecisionReservaRequestDTO request) throws Exception {
        return mockMvc.perform(patch("/api/v1/reservas/{id}/decision", reservaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private DecisionReservaRequestDTO confirmar() {
        return new DecisionReservaRequestDTO(DecisionReserva.CONFIRMAR, LocalDateTime.now().plusMinutes(45), null, null);
    }

    private DecisionReservaRequestDTO rechazar(MotivoRechazo motivo, String comentario) {
        return new DecisionReservaRequestDTO(DecisionReserva.RECHAZAR, null, motivo, comentario);
    }

    @Test
    @WithMockUser(roles = "COMPRADOR")
    @DisplayName("POST /api/v1/reservas - 201 Created")
    void crearReserva_Retorna201() throws Exception {
        when(usuarioActual.getCuentaId()).thenReturn(99L);

        ReservaRequestDTO request = new ReservaRequestDTO(
                plato.getId(),
                2,
                MedioPago.NEQUI,
                "Llegaré puntual"
        );

        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.monto").value(32000.00))
                .andExpect(jsonPath("$.plato").value("Ajiaco santafereño"));
    }

    @Test
    @WithMockUser(roles = "COMPRADOR")
    @DisplayName("POST /api/v1/reservas - 409 Conflict si no hay porciones disponibles")
    void crearReserva_SinPorciones_Retorna409() throws Exception {
        when(usuarioActual.getCuentaId()).thenReturn(99L);

        ReservaRequestDTO request = new ReservaRequestDTO(plato.getId(), 10, MedioPago.NEQUI, null);

        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "COCINERA")
    @DisplayName("Escenario 1: confirmar -> 200, CONFIRMADA, porciones conservadas y chat habilitado")
    void confirmar_debeRetornar200() throws Exception {
        decidir(reserva.getId(), confirmar())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reserva.getId().toString()))
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
                .andExpect(jsonPath("$.chatHabilitado").value(true))
                .andExpect(jsonPath("$.horaEstimadaEntrega").exists());

        Plato actualizado = platoRepository.findById(plato.getId()).orElseThrow();
        assertEquals(4, actualizado.getPorcionesDisponibles());
    }

    @Test
    @WithMockUser(roles = "COCINERA")
    @DisplayName("Escenario 2: rechazar -> 200, RECHAZADA y las porciones vuelven a la publicación")
    void rechazar_debeRetornar200YDevolverPorciones() throws Exception {
        decidir(reserva.getId(), rechazar(MotivoRechazo.INGREDIENTES_INSUFICIENTES, null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"))
                .andExpect(jsonPath("$.motivoRechazo").value("INGREDIENTES_INSUFICIENTES"))
                .andExpect(jsonPath("$.chatHabilitado").value(false));

        Plato actualizado = platoRepository.findById(plato.getId()).orElseThrow();
        assertEquals(6, actualizado.getPorcionesDisponibles());
    }

    @Test
    @WithMockUser(roles = "COCINERA")
    @DisplayName("Escenario 3: el comprador recibe un aviso con la decisión")
    void decidir_debeGenerarNotificacionParaElComprador() throws Exception {
        decidir(reserva.getId(), rechazar(MotivoRechazo.OTRO, "Se me dañó la estufa"))
                .andExpect(status().isOk());

        if (notificacionRepository != null) {
            List<Notificacion> notificaciones = notificacionRepository.findByReservaIdOrderByFechaCreacionAsc(reserva.getId());
            assertEquals(1, notificaciones.size());
            assertEquals(TipoNotificacion.RESERVA_RECHAZADA, notificaciones.get(0).getTipo());
            assertEquals(Rol.COMPRADOR, notificaciones.get(0).getRolDestinatario());
            assertEquals(42L, notificaciones.get(0).getCompradorId());
        }
    }

    @Test
    @WithMockUser(roles = "COCINERA")
    void rechazar_sinMotivo_debeRetornar400() throws Exception {
        decidir(reserva.getId(), rechazar(null, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.motivoValido").exists());
    }

    @Test
    @WithMockUser(roles = "COCINERA")
    void decidir_conCocineraAjena_debeRetornar403() throws Exception {
        lenient().when(usuarioActual.getCocineraId()).thenReturn(UUID.randomUUID());

        decidir(reserva.getId(), confirmar())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COCINERA")
    void listarPendientes_debeRetornarSoloLasVigentesDeLaCocinera() throws Exception {
        mockMvc.perform(get("/api/v1/reservas/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(reserva.getId().toString()));
    }

    @Test
    @WithMockUser(roles = "COMPRADOR")
    void obtenerPorId_debeRetornar200() throws Exception {
        when(usuarioActual.getCuentaId()).thenReturn(42L);

        mockMvc.perform(get("/api/v1/reservas/{id}", reserva.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }
}