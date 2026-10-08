package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.dtos.request.DecisionReservaRequestDTO;
import com.ollacercana.controller.dtos.request.ReservaRequestDTO;
import com.ollacercana.core.models.enums.DecisionReserva;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.models.enums.MedioPago;
import com.ollacercana.core.models.enums.MotivoRechazo;
import com.ollacercana.core.models.enums.TipoComida;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.entities.ReservaEntity;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.PlatoRepository;
import com.ollacercana.persistence.repository.ReservaRepository;
import com.ollacercana.persistence.repository.mongo.NotificacionRepository;

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

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PlatoRepository platoRepository;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private PerfilCocineraRepository perfilCocineraRepository;
    @Autowired(required = false) private NotificacionRepository notificacionRepository;
    @MockBean private UsuarioActual usuarioActual;

    private final UUID cocineraId = UUID.randomUUID();
    private PlatoEntity plato;
    private ReservaEntity reserva;

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();
        platoRepository.deleteAll();
        perfilCocineraRepository.deleteAll();

        lenient().when(usuarioActual.getCuentaId()).thenReturn(42L);
        lenient().when(usuarioActual.getCocineraId()).thenReturn(cocineraId);
        lenient().when(usuarioActual.tieneRol(any())).thenReturn(false);

        perfilCocineraRepository.save(PerfilCocineraEntity.builder()
                .id(cocineraId)
                .conjuntoResidencial("Torres del Parque")
                .verificada(true)
                .pausada(false)
                .esDestacada(false)
                .promedioCalificacion(0.0)
                .resenasPositivas(0)
                .build());

        plato = platoRepository.save(PlatoEntity.builder()
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
                .version(0)
                .build());

        LocalDateTime ahora = LocalDateTime.now();
        reserva = reservaRepository.save(ReservaEntity.builder()
                .id(UUID.randomUUID())
                .platoId(plato.getId())
                .cocineraId(cocineraId)
                .compradorId(42L)
                .cantidadPorciones(2)
                .montoTotal(new BigDecimal("32000"))
                .medioPago(MedioPago.NEQUI)
                .estado(EstadoReserva.PENDIENTE)
                .notaComprador("Sin cebolla")
                .fechaCreacion(ahora.minusMinutes(1))
                .fechaLimiteConfirmacion(ahora.plusMinutes(9))
                .version(0)
                .build());
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
                plato.getId(), 2, MedioPago.NEQUI, "Llegaré puntual");

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

        PlatoEntity actualizado = platoRepository.findById(plato.getId()).orElseThrow();
        int disponibles = actualizado.getPorcionesTotales()
                - (actualizado.getPorcionesComprometidas() == null ? 0 : actualizado.getPorcionesComprometidas());
        assertEquals(4, disponibles);
    }

        @Test
        @WithMockUser(roles = "COCINERA")
        @DisplayName("Escenario 2: rechazar -> 200, RECHAZADA y las porciones vuelven a la publicación")
        void rechazar_debeRetornar200YDevolverPorciones() throws Exception {
        // Estado ANTES: plato con 2 comprometidas. Reserva tiene 2 porciones de esas 2.
        int comprometidasAntes = platoRepository.findById(plato.getId()).orElseThrow()
                .getPorcionesComprometidas();

        decidir(reserva.getId(), rechazar(MotivoRechazo.INGREDIENTES_INSUFICIENTES, null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"))
                .andExpect(jsonPath("$.motivoRechazo").value("INGREDIENTES_INSUFICIENTES"))
                .andExpect(jsonPath("$.chatHabilitado").value(false));

        PlatoEntity actualizado = platoRepository.findById(plato.getId()).orElseThrow();
        int comprometidasDespues = actualizado.getPorcionesComprometidas() == null
                ? 0 : actualizado.getPorcionesComprometidas();

        // Las porciones se devolvieron: quedan las que había ANTES menos las de esta reserva (2).
        assertEquals(comprometidasAntes - 2, comprometidasDespues);
        // Y las disponibles aumentan
        int disponibles = actualizado.getPorcionesTotales() - comprometidasDespues;
        assertEquals(6, disponibles);
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