package com.ollacercana.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * HU-23 / OC-155: confirmación de entrega y pago para cerrar la transacción.
 */
@Schema(description = "Confirmación de entrega y pago contra entrega para cerrar la transacción (HU-23)")
public record CierreTransaccionRequestDTO(

        @Schema(description = "Confirma que la entrega se realizó", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "La confirmación de entrega es obligatoria")
        @AssertTrue(message = "Debes confirmar la entrega para cerrar la transacción")
        Boolean confirmacionEntrega,

        @Schema(description = "Confirma que el pago contra entrega se realizó", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "La confirmación de pago es obligatoria")
        @AssertTrue(message = "Debes confirmar el pago para cerrar la transacción")
        Boolean confirmacionPago,

        @Schema(description = "Comentario opcional (máx. 150 caracteres)", example = "Todo llegó caliente, gracias", maxLength = 150)
        @Size(max = 150, message = "El comentario no puede superar los 150 caracteres")
        String comentario
) {
}
