package com.ollacercana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.dtos.request.CierreTransaccionRequestDTO;
import com.ollacercana.core.models.Reserva;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.services.ICuentaService;
import com.ollacercana.core.services.ReservaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControladoresBranchesTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ReservaService reservaService;
    @MockBean private ICuentaService cuentaService;
    @MockBean private UsuarioActual usuarioActual;

    @Test
    @WithMockUser(username = "99", roles = "COMPRADOR")
    @DisplayName("ReservaController: usuario ajeno que no es admin recibe 403 al completar")
    void completar_usuarioAjeno_retorna403() throws Exception {
        UUID reservaId = UUID.randomUUID();
        Reserva r = Reserva.builder().id(reservaId).compradorId(1L).cocineraId(UUID.randomUUID()).build();

        when(reservaService.obtenerPorId(reservaId)).thenReturn(r);
        when(usuarioActual.getCuentaId()).thenReturn(99L);
        when(usuarioActual.getCocineraIdOpt()).thenReturn(Optional.empty());
        when(usuarioActual.tieneRol(Rol.ADMIN)).thenReturn(false);

        CierreTransaccionRequestDTO dto = new CierreTransaccionRequestDTO(true, true, "Ok");

        mockMvc.perform(post("/api/v1/reservas/{id}/completar", reservaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "99", roles = "COMPRADOR")
    @DisplayName("ReservaController: usuario ajeno recibe 403 al consultar por id")
    void obtenerPorId_usuarioAjeno_retorna403() throws Exception {
        UUID reservaId = UUID.randomUUID();
        Reserva r = Reserva.builder().id(reservaId).compradorId(1L).cocineraId(UUID.randomUUID()).build();

        when(reservaService.obtenerPorId(reservaId)).thenReturn(r);
        when(usuarioActual.getCuentaId()).thenReturn(99L);
        when(usuarioActual.getCocineraIdOpt()).thenReturn(Optional.empty());
        when(usuarioActual.tieneRol(Rol.ADMIN)).thenReturn(false);

        mockMvc.perform(get("/api/v1/reservas/{id}", reservaId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    @DisplayName("ReservaController: ADMIN sí puede consultar cualquier reserva")
    void obtenerPorId_admin_retorna200() throws Exception {
        UUID reservaId = UUID.randomUUID();
        Reserva r = Reserva.builder().id(reservaId).compradorId(10L).cocineraId(UUID.randomUUID()).build();

        when(reservaService.obtenerPorId(reservaId)).thenReturn(r);
        when(usuarioActual.getCuentaId()).thenReturn(1L);
        when(usuarioActual.getCocineraIdOpt()).thenReturn(Optional.empty());
        when(usuarioActual.tieneRol(Rol.ADMIN)).thenReturn(true);

        mockMvc.perform(get("/api/v1/reservas/{id}", reservaId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1", roles = "COMPRADOR")
    @DisplayName("CuentaController: PATCH /avisos actualiza correctamente")
    void cuentaController_cambiarAvisos() throws Exception {
        when(usuarioActual.getCuentaId()).thenReturn(1L);

        mockMvc.perform(patch("/api/v1/cuentas/1/avisos")
                        .param("activos", "false"))
                .andExpect(status().isOk());

        verify(cuentaService).cambiarAvisos(1L, false, 1L);
    }
}