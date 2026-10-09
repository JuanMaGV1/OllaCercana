package com.ollacercana.controller.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.ollacercana.core.models.enums.MedioPago;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para la creación o actualización del perfil de cocinera")
public class PerfilCocineraRequestDTO {

    @NotNull(message = "El ID de la cuenta es obligatorio")
    private Long cuentaId;

    @NotBlank(message = "La presentación es obligatoria")
    @Size(max = 500, message = "La presentación no puede superar los 500 caracteres")
    private String presentacion;

    @NotBlank(message = "El conjunto residencial es obligatorio")
    private String conjuntoResidencial;

    @Size(max = 5, message = "Se permiten máximo 5 especialidades")
    private List<String> especialidades;

    @NotEmpty(message = "Debe registrar al menos un medio de pago")
    @Schema(description = "Métodos de pago aceptados por la cocinera. Valores: NEQUI, DAVIPLATA, EFECTIVO, TRANSFERENCIA_BANCARIA", allowableValues = {"NEQUI", "DAVIPLATA", "EFECTIVO", "TRANSFERENCIA_BANCARIA"}, example = "[\"NEQUI\", \"EFECTIVO\"]")
    private List<MedioPago> mediosPago;

    private String numeroNequi;
    private String numeroDaviplata;
}