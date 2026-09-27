package com.ollacercana.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para verificación de teléfono mediante código OTP")
public class VerificarOtpRequestDTO {

    @NotBlank(message = "El código OTP es obligatorio")
    @Size(min = 4, max = 8, message = "El código OTP debe tener entre 4 y 8 dígitos")
    private String codigo;
}