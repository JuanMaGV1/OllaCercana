package com.ollacercana.controller.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ollacercana.core.models.enums.DecisionReserva;
import com.ollacercana.core.models.enums.MotivoRechazo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
                                                                      
@Schema(description = "Decisión de la cocinera sobre una solicitud de reserva pendiente (HU-12)")
public record DecisionReservaRequestDTO(

        @Schema(description = "Decisión tomada", example = "CONFIRMAR", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "La decisión es obligatoria (CONFIRMAR o RECHAZAR)")
        DecisionReserva decision,

        @Schema(description = "Hora estimada de entrega. Obligatoria cuando la decisión es CONFIRMAR",
                example = "2026-10-01T12:30:00")
        @Future(message = "La hora estimada de entrega debe ser posterior al momento actual")
        LocalDateTime horaEstimada,

        @Schema(description = "Motivo del rechazo. Obligatorio cuando la decisión es RECHAZAR",
                example = "INGREDIENTES_INSUFICIENTES")
        MotivoRechazo motivo,

        @Schema(description = "Comentario del rechazo (máx. 150 caracteres). Obligatorio si el motivo es OTRO",
                example = "Se me dañó la estufa", maxLength = 150)
        @Size(max = 150, message = "El comentario no puede superar los 150 caracteres")
        String comentario
) {

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "La hora estimada de entrega es obligatoria para confirmar")
    public boolean isHoraEstimadaValida() {
        return decision != DecisionReserva.CONFIRMAR || horaEstimada != null;
    }

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "El motivo es obligatorio para rechazar")
    public boolean isMotivoValido() {
        return decision != DecisionReserva.RECHAZAR || motivo != null;
    }

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "El comentario es obligatorio cuando el motivo es OTRO")
    public boolean isComentarioValido() {
        return motivo != MotivoRechazo.OTRO || (comentario != null && !comentario.isBlank());
    }
}
