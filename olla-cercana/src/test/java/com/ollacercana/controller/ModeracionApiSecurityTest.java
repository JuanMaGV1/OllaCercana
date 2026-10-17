package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.controller.dtos.request.EjecutarDecisionDTO;
import com.ollacercana.core.models.PerfilCocinera;
import com.ollacercana.core.models.Reporte;
import com.ollacercana.core.models.enums.TipoDecision;
import com.ollacercana.core.services.IModeracionService;
import com.ollacercana.config.security.UsuarioActual;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ModeracionApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IModeracionService moderacionService;

    @MockBean
    private UsuarioActual usuarioActual;

    @Test
    @WithMockUser(roles = "COMPRADOR")
    void testAccesoDenegadoParaComprador() throws Exception {
        mockMvc.perform(get("/api/v1/admin/moderacion/reportes"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("No tiene permisos para ver esta sección")));
    }

    @Test
    @WithMockUser(roles = "COCINERA")
    void testAccesoDenegadoParaCocinera() throws Exception {
        mockMvc.perform(get("/api/v1/admin/moderacion/reportes"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("No tiene permisos para ver esta sección")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: listar reportes retorna 200")
    void testAccesoPermitidoParaAdmin() throws Exception {
        when(moderacionService.obtenerReportes(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/admin/moderacion/reportes"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: detalle de reporte retorna 200")
    void testDetalleReporte() throws Exception {
        UUID reporteId = UUID.randomUUID();
        when(moderacionService.obtenerDetalle(reporteId)).thenReturn(Reporte.builder().id(reporteId).build());

        mockMvc.perform(get("/api/v1/admin/moderacion/reportes/{reporteId}", reporteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reporteId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: ejecutar decisión retorna 200")
    void testEjecutarDecision() throws Exception {
        UUID reporteId = UUID.randomUUID();
        when(usuarioActual.getCuentaId()).thenReturn(99L);

        EjecutarDecisionDTO dto = new EjecutarDecisionDTO(TipoDecision.DESCARTAR, "Sin mérito suficiente");

        mockMvc.perform(post("/api/v1/admin/moderacion/reportes/{reporteId}/decision", reporteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(moderacionService).resolver(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: listar perfiles pausados retorna 200")
    void testListarPerfilesPausados() throws Exception {
        when(moderacionService.listarPerfilesPausados()).thenReturn(List.of(
                PerfilCocinera.builder().id(UUID.randomUUID()).conjuntoResidencial("Torres del Parque").build()
        ));

        mockMvc.perform(get("/api/v1/admin/moderacion/perfiles/pausados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: reactivar perfil retorna 200")
    void testReactivarPerfil() throws Exception {
        UUID cocineraId = UUID.randomUUID();
        when(usuarioActual.getCuentaId()).thenReturn(99L);

        mockMvc.perform(post("/api/v1/admin/moderacion/perfiles/{cocineraId}/reactivar", cocineraId)
                        .content("Se superó el periodo de sanción"))
                .andExpect(status().isOk());

        verify(moderacionService).reactivarPerfil(cocineraId, "Se superó el periodo de sanción", 99L);
    }
}