package com.ollacercana.dto.response;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.RestriccionAlimentaria;
import com.ollacercana.domain.TipoComida;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PlatoResponseDTO(
    UUID id,
    String nombre,
    String descripcion,
    String fotoUrl,
    TipoComida tipoComida,
    List<RestriccionAlimentaria> restricciones,
    Integer porcionesTotales,
    Integer porcionesDisponibles,
    BigDecimal precioPorcion,
    EstadoPlato estado,
    LocalDateTime fechaPublicacion,
    LocalDateTime fechaExpiracion,
    String puntoEntrega
) {}