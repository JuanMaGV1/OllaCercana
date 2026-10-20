package com.ollacercana.controller.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.ollacercana.core.models.enums.EstadoChat;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.models.enums.MedioPago;
import com.ollacercana.core.models.enums.MotivoRechazo;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos completos y respuesta de una reserva")
public class ReservaResponseDTO {

    private UUID id;
    private UUID platoId;
    private UUID cocineraId;
    private Long compradorId;
    private Integer cantidadPorciones;
    private BigDecimal monto;
    private BigDecimal montoTotal;
    private MedioPago medioPago;
    private EstadoReserva estado;
    private String notaComprador;
    private LocalDateTime fechaCreacion;
    private LocalDateTime horaLimite;
    private LocalDateTime fechaLimiteConfirmacion;
    private LocalDateTime fechaDecision;
    private LocalDateTime horaEstimadaEntrega;
    private MotivoRechazo motivoRechazo;
    private String comentarioRechazo;
    private boolean chatHabilitado;
    private EstadoChat estadoChat;
    private LocalDateTime fechaCompletada;
    private String comentarioCierre;
    private boolean calificacionHabilitada;
    private String plato;
    private String conjunto;

    public BigDecimal getMonto() {
        return monto != null ? monto : montoTotal;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal != null ? montoTotal : monto;
    }

    public LocalDateTime getHoraLimite() {
        return horaLimite != null ? horaLimite : fechaLimiteConfirmacion;
    }

    public LocalDateTime getFechaLimiteConfirmacion() {
        return fechaLimiteConfirmacion != null ? fechaLimiteConfirmacion : horaLimite;
    }
}