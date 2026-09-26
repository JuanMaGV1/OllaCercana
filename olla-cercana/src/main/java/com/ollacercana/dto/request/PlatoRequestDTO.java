package com.ollacercana.dto.request;

import com.ollacercana.domain.RestriccionAlimentaria;
import com.ollacercana.domain.TipoComida;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record PlatoRequestDTO(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 60, message = "El nombre debe tener entre 3 y 60 caracteres")
    String nombre,

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 300, message = "La descripción debe tener entre 10 y 300 caracteres")
    String descripcion,

    @NotBlank(message = "La foto es obligatoria")
    String fotoUrl,

    @NotNull(message = "El tipo de comida es obligatorio")
    TipoComida tipoComida,

    @Size(max = 3, message = "Máximo 3 restricciones alimentarias")
    List<RestriccionAlimentaria> restricciones,

    @NotNull(message = "El número de porciones es obligatorio")
    @Min(value = 1, message = "Mínimo 1 porción")
    @Max(value = 30, message = "Máximo 30 porciones")
    Integer porcionesTotales,

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "2000.00", message = "El precio mínimo es $2.000")
    @DecimalMax(value = "50000.00", message = "El precio máximo es $50.000")
    BigDecimal precioPorcion,

    @NotNull(message = "La hora de disponibilidad es obligatoria")
    @Future(message = "La hora de disponibilidad debe ser posterior al momento actual")
    LocalDateTime horaDisponibilidad,

    @NotBlank(message = "El punto de entrega es obligatorio")
    String puntoEntrega,

    @NotNull(message = "La latitud es obligatoria")
    Double latitud,

    @NotNull(message = "La longitud es obligatoria")
    Double longitud
) {}