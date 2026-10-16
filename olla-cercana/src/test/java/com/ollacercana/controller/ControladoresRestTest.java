package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.dtos.request.CalificacionRequestDTO;
import com.ollacercana.controller.dtos.request.EnviarMensajeRequestDTO;
import com.ollacercana.controller.dtos.request.ReporteCrearDTO;
import com.ollacercana.controller.dtos.response.*;
import com.ollacercana.controller.handlers.GlobalExceptionHandler;
import com.ollacercana.core.models.enums.ObjetivoReporte;
import com.ollacercana.core.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ControladoresRestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private UsuarioActual usuarioActual;
    @Mock private ChatService chatService;
    @Mock private CalificacionService calificacionService;
    @Mock private NotificacionService notificacionService;
    @Mock private IReporteService reporteService;
    @Mock private MetricasCocineraService metricasService;
    @Mock private MedallaService medallaService;

    @InjectMocks private ChatController chatController;
    @InjectMocks private CalificacionController calificacionController;
    @InjectMocks private NotificacionController notificacionController;
    @InjectMocks private ReporteController reporteController;
    @InjectMocks private CocineraController cocineraController;
    @InjectMocks private UsuarioController usuarioController;

    private MockMvc mockMvcChat;
    private MockMvc mockMvcCalificacion;
    private MockMvc mockMvcNotificacion;
    private MockMvc mockMvcReporte;
    private MockMvc mockMvcCocinera;
    private MockMvc mockMvcUsuario;

    @BeforeEach
    void setUp() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        mockMvcChat = MockMvcBuilders.standaloneSetup(chatController).setControllerAdvice(handler).build();
        mockMvcCalificacion = MockMvcBuilders.standaloneSetup(calificacionController).setControllerAdvice(handler).build();
        mockMvcNotificacion = MockMvcBuilders.standaloneSetup(notificacionController).setControllerAdvice(handler).build();
        mockMvcReporte = MockMvcBuilders.standaloneSetup(reporteController).setControllerAdvice(handler).build();
        mockMvcCocinera = MockMvcBuilders.standaloneSetup(cocineraController).setControllerAdvice(handler).build();
        mockMvcUsuario = MockMvcBuilders.standaloneSetup(usuarioController).setControllerAdvice(handler).build();
    }

    @Test
    @DisplayName("ChatController: enviar, listar, marcar leídos y contar")
    void chatController_endpoints() throws Exception {
        UUID reservaId = UUID.randomUUID();
        EnviarMensajeRequestDTO req = new EnviarMensajeRequestDTO("Hola");
        MensajeResponseDTO res = MensajeResponseDTO.builder().texto("Hola").build();

        when(chatService.enviarMensaje(eq(reservaId), any())).thenReturn(res);
        mockMvcChat.perform(post("/api/v1/reservas/{id}/mensajes", reservaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        when(chatService.listarMensajes(eq(reservaId), any())).thenReturn(List.of(res));
        mockMvcChat.perform(get("/api/v1/reservas/{id}/mensajes", reservaId))
                .andExpect(status().isOk());

        mockMvcChat.perform(patch("/api/v1/reservas/{id}/mensajes/leidos", reservaId))
                .andExpect(status().isNoContent());

        when(chatService.contarNoLeidos(reservaId)).thenReturn(2L);
        mockMvcChat.perform(get("/api/v1/reservas/{id}/mensajes/no-leidos", reservaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noLeidos").value(2));
    }

    @Test
    @DisplayName("CalificacionController: calificar, listar y resumen")
    void calificacionController_endpoints() throws Exception {
        UUID reservaId = UUID.randomUUID();
        UUID cocineraId = UUID.randomUUID();
        CalificacionRequestDTO req = new CalificacionRequestDTO(5, "Excelente");

        when(usuarioActual.getCuentaId()).thenReturn(10L);
        when(calificacionService.calificar(eq(reservaId), eq(10L), any()))
                .thenReturn(CalificacionResponseDTO.builder().estrellas(5).build());

        mockMvcCalificacion.perform(post("/api/v1/reservas/{reservaId}/calificacion", reservaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        when(calificacionService.listarPorCocinera(eq(cocineraId), any(), eq(0), eq(10)))
                .thenReturn(PaginaResponseDTO.<CalificacionResponseDTO>builder().contenido(List.of()).build());

        mockMvcCalificacion.perform(get("/api/v1/cocineras/{cocineraId}/calificaciones", cocineraId))
                .andExpect(status().isOk());

        when(calificacionService.obtenerResumen(cocineraId))
                .thenReturn(ResumenCalificacionesDTO.builder().promedio(4.5).build());

        mockMvcCalificacion.perform(get("/api/v1/cocineras/{cocineraId}/calificaciones/resumen", cocineraId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promedio").value(4.5));
    }

    @Test
    @DisplayName("NotificacionController: listar, marcar leída y conteo")
    void notificacionController_endpoints() throws Exception {
        when(usuarioActual.getCuentaId()).thenReturn(5L);
        when(notificacionService.listarParaUsuario(5L)).thenReturn(List.of());

        mockMvcNotificacion.perform(get("/api/v1/notificaciones"))
                .andExpect(status().isOk());

        mockMvcNotificacion.perform(patch("/api/v1/notificaciones/{id}/leida", "notif-1"))
                .andExpect(status().isNoContent());

        when(notificacionService.contarNoLeidas(5L)).thenReturn(3L);
        mockMvcNotificacion.perform(get("/api/v1/notificaciones/no-leidas/conteo"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ReporteController: crear reporte y listar motivos")
    void reporteController_endpoints() throws Exception {
        ReporteCrearDTO req = ReporteCrearDTO.builder().objetivo(ObjetivoReporte.PLATO).build();
        when(usuarioActual.getCuentaId()).thenReturn(7L);
        when(reporteService.crear(any(), eq(7L))).thenReturn(ReporteDTO.builder().build());

        mockMvcReporte.perform(post("/api/v1/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvcReporte.perform(get("/api/v1/reportes/motivos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CocineraController: metricas e historial")
    void cocineraController_endpoints() throws Exception {
        UUID cocineraId = UUID.randomUUID();
        when(usuarioActual.getCocineraId()).thenReturn(cocineraId);
        when(metricasService.obtenerMetricas(eq(cocineraId), any(), any()))
                .thenReturn(MetricasCocineraResponseDTO.builder().porcionesEntregadas(10L).build());

        mockMvcCocinera.perform(get("/api/v1/cocineras/metricas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porcionesEntregadas").value(10));

        when(metricasService.obtenerHistorial(eq(cocineraId), eq(0), eq(10)))
                .thenReturn(HistorialPaginadoResponseDTO.builder().contenido(List.of()).build());

        mockMvcCocinera.perform(get("/api/v1/cocineras/historial"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("UsuarioController: listar medallas")
    void usuarioController_endpoints() throws Exception {
        when(medallaService.listarVigentes(eq(1L), any())).thenReturn(List.of());

        mockMvcUsuario.perform(get("/api/v1/usuarios/{id}/medallas", 1L))
                .andExpect(status().isOk());
    }
}